<%@ page import="java.sql.*" %>
<html>
<head>
    <style>
        li{
            list-style: none;
            background: beige;
            margin-top: 2px;
            width: 415px;
            height: auto;
            background: ;
        }
        #view{
            margin: 10px 100px;
        }
    </style>
    <script type="text/javascript">
        var webSocket = new WebSocket("ws://localhost:8080/websocket");

        webSocket.onerror = function (event) {
            makeDataOnWeb("error");
        };
        webSocket.onopen = function (event) {
           // makeDataOnWeb("conn success");
        };

        webSocket.onmessage = function (event) {
            makeDataOnWeb(event.data);
        };

        //这里只是调试用
        webSocket.onclose = function (event) {
            //makeDataOnWeb("conn close");
        };
        function makeDataOnWeb(data) {
            var a = data;
            console.log(data)
            // var divNode = document.getElementById("view");
            var liNode = document.createElement("li");
            // liNode.innerHTML = a;
            // divNode.appendChild(liNode);

            var re="";
            re='<div style="width:353px;height:auto; margin-top:3px; background: beige">'
                +  '<span style="width:20px;height:auto;background:darkkhaki; margin:3px">' + a.split(",")[0] + '</span>'
                +  '<span style="width:158px;height:auto;background:orange; margin:3px">' + a.split(",")[1] + '</span>'
                +  '<span style="width:23px;height:auto;background:coral; margin:3px">' + a.split(",")[2] + '</span>'
                +  '<span style="width:20px;height:auto;background:crimson; margin:3px">' + a.split(",")[3] + '</span>'
                +  '</div>';
            document.writeln(re);
        };

    </script>
</head>

<body>

<%@page contentType="text/html; utf8" %>
<%@page language="java" %>
<%@page import="java.sql.*" %>
<%@page pageEncoding="UTF-8" %>

<div id="view">

</div>

</body>

</html>