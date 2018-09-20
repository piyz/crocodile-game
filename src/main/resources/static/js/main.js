var roomInput;
var username = document.querySelector('#username').innerHTML;
var chatPage = document.querySelector('#chat-page');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var roomIdDisplay = document.querySelector('#room-id-display');
var guessIdDisplay = document.querySelector('#guess-id-display'); guessIdDisplay.textContent = "test"; //test content
var tableForm = document.querySelector('#table');
var unsubButton = document.querySelector('#unsub');

var guessButton1 = document.querySelector('#guess-button-id-1');
var guessButton2 = document.querySelector('#guess-button-id-2');
var guessButton3 = document.querySelector('#guess-button-id-3');

var stompClient = null;
var currentSubscription1;
var currentSubscription2;
var currentSubscription3;
var currentSubscription4;
var currentDrawSubscription;
var queueSubscription;
var path = null;

var canvasForm = document.getElementById('canvas-form');
var canvas  = document.getElementById('drawing');
var context = canvas.getContext('2d');
var width   = window.innerWidth;
var height  = window.innerHeight;

// Get the modal
var modal = document.querySelector('#myModal');
var endModal = document.querySelector('#endModal');
var modalContent = document.getElementById("modal-cont");

var drawUser = null;
var dru = document.getElementById("draw-user"); //for test

var socket = new SockJS('/ws');
stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);


document.addEventListener("DOMContentLoaded", function() {
    var mouse = [false, false, [0,0], false];

    // set canvas to full browser width/height
    //canvas.width = width;
    //canvas.height = height;

    // register mouse event handlers
    canvas.onmousedown = function(e){ mouse[0] = true; };
    canvas.onmouseup = function(e){ mouse[0] = false; };
    canvas.onmouseleave = function(e){ mouse[0] = false; };

    canvas.onmousemove = function(e) {
        // normalize mouse position to range 0.0 - 1.0
        var rect = canvas.getBoundingClientRect();
        mouse[2][0] = (e.clientX - rect.left) / width;
        mouse[2][1] = (e.clientY - rect.top) / height;
        mouse[1] = true;
    };

    // main loop, running every 25ms
    function mainLoop() {
        // check if the user is drawing
        if (mouse[0] && mouse[1] && mouse[3]) {
            // send line to to the server
            var drawMessage = JSON.stringify({
                sender : username,
                content : mouse[2].toString() + "#" + mouse[3].toString(),
                type : 'DRAW'
            });

            stompClient.send(`${path}/draw`, {}, drawMessage);
            mouse[1] = false;
        }
        mouse[3] = [mouse[2][0], mouse[2][1]];
        //mouse.pos_prev = {x: mouse.pos.x, y: mouse.pos.y};
        setTimeout(mainLoop, 25);
    }
    mainLoop();
});

function onDraw(payload){
    var message = JSON.parse(payload.body);
    context.beginPath();
    context.moveTo(message.x1 * width, message.y1 * height);
    context.lineTo(message.x2 * width, message.y2 * height);
    context.stroke();
    //0.4449152542372881,0.18916155419222905#0.4449152542372881,0.18507157464212678
}

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function unsub() {
    currentSubscription1.unsubscribe();
    currentSubscription2.unsubscribe();
    currentSubscription3.unsubscribe();
    currentSubscription4.unsubscribe();
    currentDrawSubscription.unsubscribe();

    canvasForm.classList.add('hidden');
    //canvas.classList.add('hidden');
    unsubButton.classList.add('hidden');
    tableForm.classList.remove('hidden');
    chatPage.classList.add('hidden');

    //clear chatting before
    messageArea.innerHTML = '';
}

function connecting(event) {
    roomInput = event.value;

    canvasForm.classList.remove('hidden');
    //canvas.classList.remove('hidden');
    unsubButton.classList.remove('hidden');
    tableForm.classList.add('hidden');
    chatPage.classList.remove('hidden');
    //event.preventDefault();

    enterRoom(roomInput);
}

function onConnected() {
    //connectingElement.classList.add('hidden');
    stompClient.subscribe(`/topic/table`, onChangeTable);
}

function onChangeTable() {
    $('#table').load(document.URL + ' #table')
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

// Leave the current room and enter a new one.
function enterRoom(roomId) {

    roomIdDisplay.textContent = roomId;
    path = `/app/chat/${roomId}`;

    stompClient.subscribe('/user/queue/canvas', onCanvas);
    queueSubscription = stompClient.subscribe('/user/queue/sendModal', getModalWindow);
    currentDrawSubscription = stompClient.subscribe(`/topic/${roomId}/draw`, onDraw);
    currentSubscription2 = stompClient.subscribe(`/topic/${roomId}/changeGuess`, changeGuess);
    currentSubscription1 = stompClient.subscribe(`/topic/${roomId}/public`, onMessageReceived);
    currentSubscription3 = stompClient.subscribe(`/topic/${roomId}/changeDrawUser`, onChangeDrawUser);
    currentSubscription4 = stompClient.subscribe(`/topic/${roomId}/end`, onEnd);

    stompClient.send(`${path}/addUser`,
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    );
}

function onEnd(payload) {
    var message = JSON.parse(payload.body);

    //result text
    modalContent.appendChild(document.createElement('td').appendChild(document.createTextNode(message.content)));

    guessIdDisplay.textContent = '';
    endModal.style.display = "block";

    //unsub from modal window unsub from all
    queueSubscription.unsubscribe();
    currentDrawSubscription.unsubscribe();
    currentSubscription1.unsubscribe();
    currentSubscription2.unsubscribe();
    currentSubscription3.unsubscribe();
    currentSubscription4.unsubscribe();
}

function onCanvas() {
    if (canvas.style['pointer-events'] === 'none'){
        canvas.style['pointer-events'] = "auto";
    } else {
        canvas.style['pointer-events'] = 'none';
    }
}

function onChangeDrawUser(payload) {
    var message = JSON.parse(payload.body);
    drawUser = message.sender;

    //dru.innerText = message.sender;

    //impl clear canvas
    context.clearRect(0, 0, canvas.width, canvas.height);
}

function clearCanvas() {
    context.clearRect(0, 0, canvas.width, canvas.height);
}

function getModalWindow(payload) {
    var message = JSON.parse(payload.body);

    guessButton1.textContent = message.content.split(",")[0];
    guessButton2.textContent = message.content.split(",")[1];
    guessButton3.textContent = message.content.split(",")[2];

    modal.style.display = "block";

    guessButton1.onclick = function () {
        clearInterval(downloadTimer);
        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton1.textContent}));
        modal.style.display = "none";
    };

    guessButton2.onclick = function () {
        clearInterval(downloadTimer);
        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton2.textContent}));
        modal.style.display = "none";
    };

    guessButton3.onclick = function () {
        clearInterval(downloadTimer);
        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton3.textContent}));
        modal.style.display = "none";
    };

    var timeleft = 10;
    var downloadTimer = setInterval(function(){
        document.getElementById("progressBar").value = 10 - --timeleft;
        if(timeleft <= 0){
            var random = Math.floor(Math.random() * 4);
            stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : message.content.split(",")[random]}));
            modal.style.display = "none";
            clearInterval(downloadTimer);
        }
    },1000);
}

var guess = document.getElementById("guess-window-id");
function changeGuess(payload) {
    guessIdDisplay.textContent = JSON.parse(payload.body).content;

    guess.innerHTML = '';
    var word = JSON.parse(payload.body).content;
    for (let i = 0; i < word.length; i++) {
        guess.appendChild(document.createElement('span').appendChild(document.createTextNode(word.charAt(i))));
    }
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        if (chatMessage.content === guessIdDisplay.textContent) {

            //start the game
            if (chatMessage.content === 'test'){
                stompClient.send(`/app/chat/table`, {}, JSON.stringify({content : roomInput}));
            }

            stompClient.send(`${path}/changeDrawUser`, {}, JSON.stringify({
                sender: username,
                content : drawUser,
                type: 'GUESS'
            }));
        }else {
            stompClient.send(`${path}/sendMessage`, {}, JSON.stringify(chatMessage));
        }
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
    } else if (message.type === "GUESS") {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' type right answer!';
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