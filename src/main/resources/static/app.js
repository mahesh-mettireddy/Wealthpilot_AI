document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profiler-form');
    const sections = {
        questionnaire: document.getElementById('questionnaire-section'),
        loading: document.getElementById('loading-section'),
        error: document.getElementById('error-section'),
        results: document.getElementById('results-section')
    };

    let allocationChartInstance = null;
    let projectionChartInstance = null;

    function showSection(sectionName) {
        Object.values(sections).forEach(sec => sec.classList.add('hidden'));
        sections[sectionName].classList.remove('hidden');
    }

    function showError(message) {
        document.getElementById('error-message').textContent = message;
        showSection('error');
    }

    document.getElementById('btn-retry').addEventListener('click', () => {
        showSection('questionnaire');
    });

    document.getElementById('btn-start-over').addEventListener('click', () => {
        form.reset();
        showSection('questionnaire');
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const formData = new FormData(form);
        const reqData = {
            age: parseInt(formData.get('age')),
            monthlyIncome: parseFloat(formData.get('monthlyIncome')),
            goalType: formData.get('goalType'),
            investmentHorizonYears: parseInt(formData.get('investmentHorizonYears')),
            monthlyInvestmentAmount: parseFloat(formData.get('monthlyInvestmentAmount')),
            riskComfort: formData.get('riskComfort'),
            existingInvestmentsPctEquity: 0
        };

        showSection('loading');

        try {
            // 1. Risk Profile
            const riskRes = await fetch('/api/risk-profile', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(reqData)
            });
            const riskData = await riskRes.json();
            if (!riskData.success) throw new Error(riskData.error || 'Failed to calculate risk profile');

            const riskCategory = riskData.data.riskCategory;

            // 2. Recommend Allocation
            const recommendRes = await fetch('/api/recommend', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    riskCategory: riskCategory,
                    goalType: reqData.goalType,
                    investmentHorizonYears: reqData.investmentHorizonYears,
                    monthlyInvestmentAmount: reqData.monthlyInvestmentAmount
                })
            });
            const recommendData = await recommendRes.json();
            if (!recommendData.success) throw new Error(recommendData.error || 'Failed to generate recommendation');

            const allocation = recommendData.data.allocation;
            
            // 3. Goal Projection
            const projectRes = await fetch('/api/project', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    allocation: allocation,
                    monthlyInvestmentAmount: reqData.monthlyInvestmentAmount,
                    investmentHorizonYears: reqData.investmentHorizonYears
                })
            });
            const projectData = await projectRes.json();
            if (!projectData.success) throw new Error(projectData.error || 'Failed to calculate projection');

            renderResults(riskCategory, recommendData.data, projectData.data);
            showSection('results');

        } catch (err) {
            showError(err.message);
        }
    });

    function renderResults(riskCategory, recommend, project) {
        // Badge
        const badge = document.getElementById('risk-badge');
        badge.textContent = riskCategory;
        badge.style.backgroundColor = `var(--badge-${riskCategory.toLowerCase()})`;

        // Rationale
        document.getElementById('rationale-text').textContent = recommend.rationale;

        // Funds List
        const fundsList = document.getElementById('funds-list');
        fundsList.innerHTML = '';
        recommend.recommendedFunds.forEach(fund => {
            const li = document.createElement('li');
            li.innerHTML = `<span class="fund-name">${fund.name}</span><span class="fund-alloc">${fund.allocationPct}%</span>`;
            fundsList.appendChild(li);
        });

        // Summary Stats
        document.getElementById('stat-invested').textContent = formatCurrency(project.totalInvested);
        document.getElementById('stat-corpus').textContent = formatCurrency(project.projectedCorpus);
        document.getElementById('stat-return').textContent = `${project.blendedAnnualReturnPct}%`;

        // Charts
        renderAllocationChart(recommend.allocation);
        renderProjectionChart(project.yearlyBreakdown);
    }

    function renderAllocationChart(allocation) {
        const ctx = document.getElementById('allocationChart').getContext('2d');
        if (allocationChartInstance) allocationChartInstance.destroy();
        
        allocationChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Equity', 'Debt', 'Gold'],
                datasets: [{
                    data: [allocation.equityPct, allocation.debtPct, allocation.goldPct],
                    backgroundColor: ['#4f46e5', '#10b981', '#f59e0b'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom' }
                },
                cutout: '70%'
            }
        });
    }

    function renderProjectionChart(breakdown) {
        const ctx = document.getElementById('projectionChart').getContext('2d');
        if (projectionChartInstance) projectionChartInstance.destroy();

        const labels = breakdown.map(b => `Year ${b.year}`);
        const data = breakdown.map(b => b.corpus);

        projectionChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Projected Value (INR)',
                    data: data,
                    borderColor: '#4f46e5',
                    backgroundColor: 'rgba(79, 70, 229, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return '₹' + (value / 100000).toFixed(1) + 'L';
                            }
                        }
                    }
                }
            }
        });
    }

    function formatCurrency(num) {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(num);
    }
});
