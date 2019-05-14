<?php
    $con = mysqli_connect('beedbinstance.clo4pm1gtgey.us-east-2.rds.amazonaws.com', 'devfon', 'pw', 'registration');
     //안드로이드 앱으로부터 아래 값들을 받음
    $userID = $_POST["userID"];
    $userPassword = $_POST["userPassword"];
    $statement = mysqli_prepare($con, "SELECT userID FROM USER WHERE userID = ? AND userPassword = ?");
    mysqli_stmt_bind_param($statement, "ss", $userID, $userPassword);
    mysqli_stmt_execute($statement);
    mysqli_stmt_store_result($statement);
    mysqli_stmt_bind_result($statement, $userID);
    $response = array();
    $response["success"] = false;
    while(mysqli_stmt_fetch($statement)){
      $response["success"] = true;
      $response["userID"] = $userID;
    }
    echo json_encode($response);
?>