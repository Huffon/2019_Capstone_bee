// MySQL, express 모듈 사용하겠다고 정의
const mysql = require('mysql');
const express = require('express');
const app = express();
// POST method로 넘어온 body parsing하기 위한 모듈 사용
const bodyParser = require('body-parser');

// bodyParser의 json 형식 사용
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Node.js 서버가 정상적으로 작동하기 위해 확인하기 위한 index log
app.get('/', function(req, res) {
    response.send('Hello Bee Database!');
})

app.listen(80, function () {
    console.log('Bee Database server is running . . .');
});

// AWS RDS에 접속하기 위한 설정
var connection = mysql.createConnection({
    host: "beedbinstance.clo4pm1gtgey.us-east-2.rds.amazonaws.com",
    user: "devfon",
    database: "registration",
    password: "gjgns624853",
    port: 3306
});

// 회원 가입을 위한 function
app.post('/user/register', function (req, res) {
    // 모바일 App에서 넘어온 Request body에서 정보 추출
    var id = req.body.userID;
    var pw = req.body.userPassword;
    var gender = req.body.userGender;
    var email = req.body.userEmail; 

    // SQL 생성
    var sql = 'INSERT INTO USER (userID, userPassword, userGender, UserEmail) VALUES (?, ?, ?, ?)';
    // SQL의 '?' 부에 들어갈 변수 리스트 생성
    var params = [id, pw, gender, email];
    console.log(sql);

    // SQL 수행
    connection.query(sql, params, function (err, result) {
        if (err){
            console.log(err);
        }
        // 성공적으로 회원가입에 성공하는 경우
        else {
            res.json({
                success: true
            })
        }
    });
});

// 아이디 중복 확인을 위한 function
app.post('/user/validate', function (req, res) {
    // 모바일 App에서 넘어온 Request body에서 정보 추출
    var id = req.body.userID;

    // SQL 생성
    var sql = 'SELECT userID FROM USER WHERE userID = ?';
    console.log(sql);

    // SQL 수행
    connection.query(sql, id, function (err, result) {
        if (err)
            console.log(err);
        else {
            // 사용자가 입력한 ID가 데이터베이스에 이미 존재하는 경우
            if (result.length === 1) {
                res.json({
                    success: false
                });
            } 
            // 사용자가 입력한 ID가 데이터베이스에 존재하지 않는 경우
            else {
                res.json({
                    success: true
                })
            }
        }
    });
});

// 로그인을 위한 function
app.post('/user/login', function (req, res) {
    // 모바일 App에서 넘어온 Request body에서 정보 추출
    var id = req.body.userID;
    var pw = req.body.userPassword;

    // SQL 생성
    var sql = 'SELECT * FROM USER WHERE userID = ?'
    console.log(sql);

    // SQL 수행
    connection.query(sql, id, function (err, result) {
        if (err)
            console.log(err)
        else {
            // 사용자가 입력한 계정에 해당하는 row가 없을 경우
            if (result.length === 0) {
                res.json({
                    success: false,
                    msg: '해당 사용자가 존재하지 않습니다.'
                });
            } 
            // 사용자가 입력한 계정과 암호가 불일치 하는 경우
            else if (pw !== result[0].userPassword) {
                res.json({
                    success: false,
                    msg: '사용자 암호를 다시 확인해주세요.'
                });
            } 
            // 정상적으로 로그인이 수행된 경우
            else {
                res.json({
                    success: true,
                    msg: '로그인에 성공하였습니다.'
                });
            }
        }
    })
});