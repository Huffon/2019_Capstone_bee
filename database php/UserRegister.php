<?php
     $con = mysqli_connect('beedbinstance.clo4pm1gtgey.us-east-2.rds.amazonaws.com', 'devfon', 'pw', 'registration');
     //안드로이드 앱으로부터 아래 값들을 받음

     $userID = $_POST["userID"];
     $userPassword = $_POST["userPassword"];
     $userGender =  $_POST["userGender"];
     $userEmail = $_POST["userEmail"];


     //insert 쿼리문을 실행함
     $statement = mysqli_prepare($con, "INSERT INTO USER VALUES ('$userID', '$userPassword', '$userGender', '$userEmail')");
     mysqli_stmt_execute($statement);
     $response = array();
     $response["success"] = true;
     $response["contents"] = $userPassword;
     //회원 가입 성공을 알려주기 위한 부분임
     echo json_encode($response);
?>