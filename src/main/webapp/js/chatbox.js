/**
 * AI Chatbox — HandMade Craft
 * Frontend logic for the floating AI chat widget
 */
(function () {
    'use strict';

    const contextPath = document.body.getAttribute('data-context-path') || window.contextPath || '';
    const API_URL = contextPath + '/api/chatbot';

    // DOM elements
    const toggle = document.getElementById('chatbox-toggle');
    const chatWindow = document.getElementById('chatbox-window');
    const messagesContainer = document.getElementById('chatbox-messages');
    const inputField = document.getElementById('chatbox-input');
    const sendBtn = document.getElementById('chatbox-send');
    const closeBtn = document.getElementById('chatbox-close');
    const badge = document.getElementById('chatbox-badge');

    let isOpen = false;
    let isFirstOpen = true;
    let isSending = false;

    // --- Toggle chat window ---
    function toggleChat() {
        isOpen = !isOpen;
        chatWindow.classList.toggle('open', isOpen);
        toggle.classList.toggle('active', isOpen);

        if (isOpen) {
            if (badge) badge.style.display = 'none';
            if (isFirstOpen) {
                showWelcomeMessage();
                isFirstOpen = false;
            }
            setTimeout(() => inputField.focus(), 350);
        }
    }

    toggle.addEventListener('click', toggleChat);
    closeBtn.addEventListener('click', toggleChat);

    // --- Welcome message ---
    function showWelcomeMessage() {
        appendMessage('bot',
            'Xin chào! 👋 Tôi là trợ lý AI của <b>HandMade Craft</b>. ' +
            'Tôi có thể giúp bạn tìm sản phẩm, tư vấn mua hàng, hoặc giải đáp thắc mắc. Hãy hỏi tôi bất cứ điều gì nhé!',
            true
        );
    }

    // --- Append message to chat ---
    function appendMessage(role, text, withQuickActions) {
        const msgDiv = document.createElement('div');
        msgDiv.className = 'chat-msg ' + role;

        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'chat-msg-avatar';
        avatarDiv.innerHTML = role === 'bot'
            ? '<i class="fas fa-robot"></i>'
            : '<i class="fas fa-user"></i>';

        const bubbleDiv = document.createElement('div');
        bubbleDiv.className = 'chat-msg-bubble';
        bubbleDiv.innerHTML = formatMessage(text);

        msgDiv.appendChild(avatarDiv);
        msgDiv.appendChild(bubbleDiv);
        messagesContainer.appendChild(msgDiv);

        // Quick action buttons (only on welcome)
        if (withQuickActions) {
            const quickDiv = document.createElement('div');
            quickDiv.className = 'chat-quick-actions';
            quickDiv.style.alignSelf = 'flex-start';
            quickDiv.style.marginLeft = '38px';

            const questions = [
                'Sản phẩm bán chạy?',
                'Chính sách đổi trả?',
                'Phí vận chuyển?',
                'Liên hệ cửa hàng'
            ];

            questions.forEach(q => {
                const btn = document.createElement('button');
                btn.className = 'chat-quick-btn';
                btn.textContent = q;
                btn.addEventListener('click', () => {
                    inputField.value = q;
                    sendMessage();
                    quickDiv.remove();
                });
                quickDiv.appendChild(btn);
            });

            messagesContainer.appendChild(quickDiv);
        }

        scrollToBottom();
    }

    // --- Format message text ---
    function formatMessage(text) {
        // Convert **bold** to <b>
        text = text.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
        // Convert *italic* to <i>
        text = text.replace(/\*(.*?)\*/g, '<i>$1</i>');
        // Convert newlines to <br>
        text = text.replace(/\n/g, '<br>');
        return text;
    }

    // --- Show typing indicator ---
    function showTyping() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chat-typing';
        typingDiv.id = 'chatbox-typing';

        typingDiv.innerHTML = `
            <div class="chat-msg-avatar"><i class="fas fa-robot"></i></div>
            <div class="typing-dots">
                <span></span><span></span><span></span>
            </div>
        `;

        messagesContainer.appendChild(typingDiv);
        scrollToBottom();
    }

    function removeTyping() {
        const el = document.getElementById('chatbox-typing');
        if (el) el.remove();
    }

    // --- Send message ---
    async function sendMessage() {
        const text = inputField.value.trim();
        if (!text || isSending) return;

        isSending = true;
        sendBtn.disabled = true;
        inputField.value = '';

        // Show user message
        appendMessage('user', escapeHtml(text));

        // Show typing
        showTyping();

        try {
            const res = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text })
            });

            removeTyping();

            if (!res.ok) throw new Error('Network error');

            const data = await res.json();
            appendMessage('bot', data.reply || 'Xin lỗi, tôi không hiểu. Bạn có thể hỏi lại không?');

        } catch (err) {
            removeTyping();
            appendMessage('bot', 'Xin lỗi, đã xảy ra lỗi kết nối. Vui lòng thử lại sau. 😔');
            console.error('Chatbot error:', err);
        } finally {
            isSending = false;
            sendBtn.disabled = false;
            inputField.focus();
        }
    }

    // --- Event listeners ---
    sendBtn.addEventListener('click', sendMessage);
    inputField.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // --- Utilities ---
    function scrollToBottom() {
        requestAnimationFrame(() => {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });
    }

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }
})();
