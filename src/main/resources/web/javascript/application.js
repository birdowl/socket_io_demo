$(function () {
    "use strict";

    var detect = $('#detect');
    var header = $('#header');
    var content = $('#content');
    var input = $('#input');
    var status = $('#status');
    var myName = false;

    var discoverBtn = $('#discoverBtn');
    var discoverMessage = $('#discoverMessage');
    var discoverStatus = $('#discoverStatus');
    var discoverProgress = $('#discoverProgress');

    // uncomment the following line to make this application connect to http://lab.happygears.net:8080/socket.io
    // var socket = io.connect('http://lab.happygears.net:8080/', {'resource': 'socket.io'});   //  {'resource':loginurl + 'socket.io'});

    // this makes it connect back to the same server and port it has been loaded from
//    var socket = io.connect('http://localhost:9100', {'resource': 'socket.io'});   //  {'resource':loginurl + 'socket.io'});
    var socket = io.connect('http://localhost:9100/v2');

    socket.on('connect', function () {
        content.html($('<p>', { text: 'Socket.IO connected'}));
        input.removeAttr('disabled').focus();
        status.text('Choose name:');

        socket.on('chat message', message);

        socket.on('error', function (e) {
            content.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
                + 'socket or the server is down' }));
        });

    discoverBtn.click(function(e) {
        socket.emit('discover');
    });

    socket.on('status', function(status) {
        discoverBtn.html(status.buttonText);
        discoverMessage.html(status.message);
        discoverStatus.html(status.statusMessage);

        if(status.statusCode == 0)
            discoverStatus.removeClass('progress').addClass('success');
        else
        if(status.statusCode == 1)
           discoverStatus.removeClass('success').addClass('progress');

        if(status.operationInProgress)
            discoverProgress.show();
        else
            discoverProgress.hide();
    });

    input.keydown(function(e) {
        if (e.keyCode === 13) {
            var msg = $(this).val();

            if (myName === false)
            {
                myName = msg;
                status.text(myName + ': ').css('color', 'blue');
                input.removeAttr('disabled').focus();
            }
            else
            if(msg == '/cnj')
            {
                socket.emit('cnj');
            }
            else
            {
                input.attr('disabled', 'disabled');
                socket.emit('chat message', { author: myName, text: msg, time: new Date() }, function() {
                    addMessage(myName, msg, 'blue', new Date());
                    input.removeAttr('disabled').focus();
                });

            }
            $(this).val('');
        }
    });

    function message(msg) {
       input.removeAttr('disabled');
        var date = typeof(msg.time) == 'string' ? new Date(Date.parse(msg.time)) : msg.time;
        addMessage(msg.author, msg.text, 'black', date);
    }

    function addMessage(author, message, color, datetime) {
        content.append('<p><span style="color:' + color + '">' + author + '</span> @ ' +
            + (datetime.getHours() < 10 ? '0' + datetime.getHours() : datetime.getHours()) + ':'
            + (datetime.getMinutes() < 10 ? '0' + datetime.getMinutes() : datetime.getMinutes())
            + ': ' + message + '</p>');
    }

});

})

