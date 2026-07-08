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

            chatContext.riskProfile = riskData.data;
            chatContext.recommendedPortfolio = recommendData.data;
            chatContext.projection = projectData.data;
            chatContext.history = [];
            
            // Show chat widget and results
            chatWidget.classList.remove('hidden');
            chatWidget.classList.remove('collapsed');
            
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

    // --- Chat Co-pilot Logic ---
    let chatContext = {
        riskProfile: null,
        recommendedPortfolio: null,
        projection: null,
        history: []
    };

    const chatWidget = document.getElementById('chat-widget');
    const btnToggleChat = document.getElementById('btn-toggle-chat');
    const chatBody = document.getElementById('chat-body');
    const chatInput = document.getElementById('chat-input');
    const btnSendChat = document.getElementById('btn-send-chat');

    btnToggleChat.addEventListener('click', () => {
        chatWidget.classList.toggle('collapsed');
        btnToggleChat.textContent = chatWidget.classList.contains('collapsed') ? '^' : '_';
    });

    btnSendChat.addEventListener('click', sendChatMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendChatMessage();
    });

    async function sendChatMessage() {
        const text = chatInput.value.trim();
        if (!text) return;

        appendMessage('user', text);
        chatInput.value = '';
        chatInput.disabled = true;
        btnSendChat.disabled = true;

        // Show typing indicator
        const typingId = 'typing-' + Date.now();
        appendMessage('assistant', '...', typingId);

        try {
            const res = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    message: text,
                    history: chatContext.history,
                    riskProfile: chatContext.riskProfile,
                    recommendedPortfolio: chatContext.recommendedPortfolio,
                    projection: chatContext.projection
                })
            });
            
            const data = await res.json();
            document.getElementById(typingId).remove();

            if (data.success && data.data && data.data.reply) {
                appendMessage('assistant', data.data.reply);
                chatContext.history.push({ role: 'user', content: text });
                chatContext.history.push({ role: 'assistant', content: data.data.reply });
            } else {
                appendMessage('assistant', 'Sorry, I encountered an error answering that.');
            }
        } catch (err) {
            document.getElementById(typingId).remove();
            appendMessage('assistant', 'Network error while contacting the advisor.');
        } finally {
            chatInput.disabled = false;
            btnSendChat.disabled = false;
            chatInput.focus();
        }
    }

    function appendMessage(role, text, id = null) {
        const div = document.createElement('div');
        div.className = `chat-message ${role}`;
        if (id) div.id = id;
        div.innerHTML = `<p>${text}</p>`;
        chatBody.appendChild(div);
        chatBody.scrollTop = chatBody.scrollHeight;
    }
});
