const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const cookieParser = require('cookie-parser');
const QRCode = require('qrcode');
const app = express();
const randomString = require('randomized-string');
const nodemailer = require('nodemailer')
const twilio = require('twilio')
const dotenv = require('dotenv')
const admin = require('firebase-admin')
const credentials = require('./firebase_KEY.json')

dotenv.config() //konfiguracja env dla Twillio

//inicjalizacja firebase
admin.initializeApp({
  credential: admin.credential.cert(credentials)
});
module.exports = admin;

//user data
const Email = "test@test.pl";
const Password = "P@ssw0rd";
var PhoneNumber = null;
var has2FAApp = false;

//mobile data
var fcmToken = null;

//is user verified by 2FA?
var QRVerified = false;
var biometrVerified = false

//codes for verification
var emailCode = null;
var smsCode = null;
var QRCode2FA = null;
var numberCode = null;

//connection codes for mobile app
var connCode = null;
let qrCode = "fghjk45678cvbn12345rtyu"

app.use(cookieParser());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:false}));
app.use(express.json());

//ZAPORA BLOKUJE DOSTĘP PRZEZ SIEĆ Z INNEGO URZĄDZENIA
const corsOptions = {
  //origin: 'http://localhost:3000',
  origin: true,
  credentials: true,
  optionSuccesStatus: 200
}
app.use(cors(corsOptions));

app.get('/', (req, res) => {
  res.send("Server site");
  res.end();
});

//logowanie użytkownika
app.post("/login", (req, res) => {
  const { email, password } = req.body;
  QRVerified = false;
  biometrVerified = false;

  if (email === Email && password === Password)
  {
    res.cookie('loginToken', 'LoginToken', {
      httpOnly: true,
      maxAge: 5*60*1000,
      sameSite: 'lax',
    });
    return res.json({status: "ok"});
  } else {
    return res.status(401).json({ status: "brak dostępu"});
  }
});
//sprawdzenie danych użytkownika
app.get("/checkUser", (req, res) => {
  const token = req.cookies.loginToken;
  if (token === 'LoginToken') {
    return res.json({
       loggedIn: true,
       hasPhone: PhoneNumber !== null,
       has2FA: has2FAApp === true
     });
  } else {
    return res.json({
       loggedIn: false,
       hasPhone: false,
       has2FA: false
     });
  }
});
//wylogowanie użytkownika, usunięcie cookies i reset weryfikacji
app.post("/logout", (req, res) => {
  res.clearCookie('loginToken');
  QRVerified = false;
  biometrVerified = false;
  res.json({ status: "Wylogowano" });
});
app.post("/setPhoneNumber", (req, res) => {
  PhoneNumber = req.body.phoneNumber;
  //console.log(PhoneNumber);
  if( PhoneNumber != null) return res.json({status: "ok"});
  else { return res.json({status: "Błąd"})};
});
//generowanie kodu QR w formacie base64
app.get("/qrCode", (req,res) => {
  QRCode.toDataURL(qrCode, function (err, code) {
    if(err) return console.log("error");
    return res.json(code);
  });
});
//generowanie kodu połaczeniowego z aplikacją mobilną
app.get("/connCode", (req, res) => {
  connCode = randomString.generate(7);
  console.log("ConnCode: " + connCode);
  return res.json(connCode);
});
app.get("/check2FA", (req, res) => {
  if(has2FAApp) return res.json(true);
  return res.json(false);
});
//wysłanie emaila poprze nodemailer z wygenerowanym kodem do weryfikacji
app.get("/sendEmail", (req, res) => {
  emailCode = randomString.generate(8);

  //tworzenie konta testowego
  nodemailer.createTestAccount((err, account) => {
  if (err) {
    console.error("Failed to create a testing account. " + err.message);
    return;
  }

  const transporter = nodemailer.createTransport({
    host: "smtp.ethereal.email",
    port: 587,
    secure: false, // upgrade later with STARTTLS
    auth: {
      user: account.user, // generated user
      pass: account.pass, // generated password
    },
    tls: {
      rejectUnauthorized: false
    }
  });

  transporter
  .sendMail({
    from: "TEST <no-reply@test.pl>",
    to: Email,
    subject: "Code for verification",
    text: "Your code for veryfication is: " + emailCode
  })
  .then((info) => {
    console.log("Message sent: %s", info.messageId);
    // Preview the stored message in Ethereal’s web UI
    console.log("Preview URL: %s", nodemailer.getTestMessageUrl(info));
  })
  .catch(console.error);
  });
  return res.json({message: "success"});
});
app.get("/getEmailCode", (req, res) => {
  return res.json({ServerEmailCode: emailCode});
});
app.get("/getSMSCode", (req, res) => {
  return res.json({ServerSMSCode: smsCode});
});
//wysłanie SMS - patrz na dole kodu
app.get("/sendSMS", (req, res) => {
  sendSMS();
  return res.json({message: "success"});
});
//wysłanie i generowanie kodu QR w formacie Base64 
app.get("/QRVerification", (req, res) => {
  QRCode2FA = randomString.generate(6);
  var QRCode2FAJSON = JSON.stringify(QRCode2FA);
  //console.log(QRCode2FA);
  QRCode.toDataURL(QRCode2FAJSON, function(err, code) {
    if(err) return console.log("ERROR");
    return res.json(code);
  });
});
//check if user is verified by QR mobile app
app.get("/checkQR", (req, res) => {
 return res.json({checkQR: QRVerified});
});
app.get("/verifiNumberCode", (req,res) => {
 return res.json({ code: numberCode });
});
app.get("/checkBiometric", (req, res) => {
 return res.json({ biometricVerification: biometrVerified })
});
//wysłanie powiadomienie do aplikacji mobilnej z prośbą o logownaie biometryczne
app.post("/sendLoginRequest", async (req, res) => {
  const userID = req.body;

  const message = {
    token: fcmToken,
    notification: {
      title: 'Zaloguj się',
      body: 'Kliknij aby się zalogować',
    },
    data: {
      screen: 'BiometricLogin',
      userId: userID.userID, 
    },
  };

  try {
  await admin.messaging().send(message);
  res.status(200).send({ success: true });
  } catch ( error) {
    console.log("FCM error: " + error);
    res.status(500).send({ success: false, error: error.message });
  }
});

//MOBILE APP
app.get("/getEmail", (req, res) => {
  return res.json({ email: Email });
});
app.get("/getQRConnCode", (req, res) => {
  return res.json({ connectionQRCode: qrCode })
});
app.get("/getConnCode", (req, res) => {
  return res.json({ connectionCode: connCode })
});
//wygeneruj i pobierz 6-cyfrowy kod
app.get("/getNumberCode", (req, res) => {
  numberCode = Math.floor(100000 + Math.random() * 900000);
  return res.json({ code: numberCode });
});
app.get("/getQRCode", (req, res) => {
  return res.json({qrCode: QRCode2FA})
});
app.post("/set2FAApp", (req, res) => {
  if (req.body) {
    has2FAApp = true;
    res.json({success: true});
  }
  else {
    has2FAApp = false;
    res.json({success: false});
  }
  console.log("Has2FA: " + has2FAApp)
});
app.post("/setQRVerified", (req, res) => {
  if(req.body) {  
    QRVerified = true;
    res.json({success: true});
  }
  else {
    QRVerified = false;
    res.json({success: false});
  }
  console.log(QRVerified)
});
app.post("/setFCMToken", (req, res) => {
  fcmToken = req.body.token;
  res.status(200).send({ success: true });
  //console.log("FCM Token: ")
  //console.log(fcmToken)
});
app.post("/setBiometricVerified", (req, res) => {
  if(req.body) {  
    biometrVerified = true;
    res.json({success: true});
  }
  else {
    biometrVerified = false;
    res.json({success: false});
  }
  console.log("BiometricVerified: " + biometrVerified)
});

const port = 4000;
const server = app.listen(port, '0.0.0.0', () => {
  console.log('Server is running on port: ' + port);
});

//Funkcja do wysysłania SMS poprzez Twillio
async function sendSMS() {
  const client = new twilio(process.env.TWILIO_SID, process.env.TWILIO_AUTH_TOKEN);
  smsCode = randomString.generate(5);

  return client.messages
  .create({body: 'Twój kod do weryfikacji: ' + smsCode, from:process.env.PHONE_NUMBER, to: PhoneNumber})
  .then(message => console.log(message, "Message sent"))
  .catch(err => console.log(err, "Message not sent"));
}