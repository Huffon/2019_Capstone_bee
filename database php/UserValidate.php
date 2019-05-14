<?php
    $con = mysqli_connect('beedbinstance.clo4pm1gtgey.us-east-2.rds.amazonaws.com', 'devfon', 'pw', 'registration');
     $userID = $_POST["userID"];
     $statement = mysqli_prepare($con, "SELECT userID FROM USER WHERE userID = ?");
     //위에서 *로 하면 mysqli_stmt_bind_result에서 에러가 나서 정정함
     mysqli_stmt_bind_param($statement, "statement", $userID);
     mysqli_stmt_execute($statement);
     mysqli_stmt_store_result($statement);//결과를 클라이언트에 저장함
     mysqli_stmt_bind_result($statement, $userID);//결과를 $userID에 바인딩함

     $response = array();
     $response["success"] = true;

     while(mysqli_stmt_fetch($statement)){
       $response["success"] = false;//회원가입불가를 나타냄
       $response["userID"] = $userID;
     }
     echo json_encode($response);
?>