
var roomInput;
var username = document.querySelector('#username').innerHTML;
var chatPage = document.querySelector('#chat-page');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var roomIdDisplay = document.querySelector('#room-id-display');
var guessIdDisplay = document.querySelector('#guess-id-display'); guessIdDisplay.textContent = "test";
var tableForm = document.querySelector('#table');
var unsubButton = document.querySelector('#unsub');

var guessButton1 = document.querySelector('#guess-button-id-1');
var guessButton2 = document.querySelector('#guess-button-id-2');
var guessButton3 = document.querySelector('#guess-button-id-3');

var stompClient = null;
var currentSubscription;
var path = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function unsub() {
    currentSubscription.unsubscribe();

    unsubButton.classList.add('hidden');
    tableForm.classList.remove('hidden');
    chatPage.classList.add('hidden');

    //clear chatting before
    messageArea.innerHTML = '';
}

function connect(event) {
    roomInput = event.value;

    unsubButton.classList.remove('hidden');
    tableForm.classList.add('hidden');
    chatPage.classList.remove('hidden');

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
    //event.preventDefault();
}

// Leave the current room and enter a new one.
function enterRoom(roomId) {

    roomIdDisplay.textContent = roomId;
    path = `/app/chat/${roomId}`;

    stompClient.subscribe(`/topic/${roomId}/changeGuess`, changeGuess); //impl unsubscribe
    currentSubscription = stompClient.subscribe(`/topic/${roomId}`, onMessageReceived);
    stompClient.subscribe('/user/queue/sendModal', getModalWindow);

    stompClient.send(`${path}/addUser`,
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    );
}

function getModalWindow(payload) {
    var message = JSON.parse(payload.body);

    guessButton1.textContent = message.content.split(",")[0];
    guessButton2.textContent = message.content.split(",")[1];
    guessButton3.textContent = message.content.split(",")[2];

    modal.style.display = "block";
}

guessButton1.onclick = function () {
    stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton1.textContent}));
    modal.style.display = "none";
};

guessButton2.onclick = function () {
    stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton2.textContent}));
    modal.style.display = "none";
};

guessButton3.onclick = function () {
    stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton3.textContent}));
    modal.style.display = "none";
};

function changeGuess(payload) {
    guessIdDisplay.textContent = JSON.parse(payload.body).content;
}

// Get the modal
var modal = document.querySelector('#myModal');
// Get the button that opens the modal
//var btn = document.getElementById("myBtn");
// Get the <span> element that closes the modal
//var span = document.getElementsByClassName("close")[0];
// When the user clicks on the button, open the modal
/*
btn.onclick = function() {
    modal.style.display = "block";
};
 */
// When the user clicks on <span> (x), close the modal
/*
span.onclick = function() {
    modal.style.display = "none";
};
 */
// When the user clicks anywhere outside of the modal, close it
/*
window.onclick = function(event) {
    if (event.target === modal) {
        modal.style.display = "none";
    }
};
 */

function onConnected() {
    enterRoom(roomInput);
    connectingElement.classList.add('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent.startsWith('/join ')) {
        var newRoomId = messageContent.substring('/join '.length);
        enterRoom(newRoomId);
        while (messageArea.firstChild) {
            messageArea.removeChild(messageArea.firstChild);
        }
    } else if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send(`${path}/sendMessage`, {}, JSON.stringify(chatMessage));
    }
    messageInput.value = '';
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else if (message.content === guessIdDisplay.textContent) {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' type right answer!';
        stompClient.send(`${path}/sendModal`, {}, JSON.stringify({sender: username}));
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

messageForm.addEventListener('submit', sendMessage, true);