import { useNavigate } from "react-router-dom";
import { useEffect } from "react";

const Login2FA = () => {
  const navigate = useNavigate();

  const handleClick = async () => {
     try {
          const logout = await fetch("http://localhost:4000/logout", {
              method: 'POST',
              credentials: "include",
          });
          if(!logout.ok) throw new Error ("HTTP Error: " + logout.status)
      } catch(error) { alert("error: " + error); }
      window.location.reload();
  };

  const sendEMail = async () => {
    try {
      const response = await fetch("http://localhost:4000/sendEmail");
      if(!response.ok) throw new Error('HTTP error' + response.status);
      navigate("/EmailCode")
    } catch(error) { alert("Error: " + error); }
  };
  const sendSMS = async () => {
    try {
      const response = await fetch("http://localhost:4000/sendSMS");
      if(!response.ok) throw new Error('HTTP error' + response.status);
      navigate("/SMSCode")
    } catch(error) { alert("Error: " + error); }
  };
  const sendMobileCode = async () => {
    navigate("/AppCodeVerification")
  };
  const sendQRCodeVer = async () => {
    try {
      const response = await fetch("http://localhost:4000/QRVerification");
      if(!response.ok) throw new Error('HTTP error: ' + response.status);
      const QRCode = await response.json();
      navigate("/QRCodeVerification", {
      state: QRCode
    });
    } catch(error) { alert("Error: " + error); }
  };
  const sendBioNotifi = async () => {
    try {
      const send = await fetch("http://localhost:4000/sendLoginRequest", {
        method: "POST",
        headers: { "Content-Type":"application/json" },
        body: JSON.stringify({
          userID: "1234"
        })
      });
      if (!send.ok) throw new Error('HTTP error ' + send.status);
    } catch(error) { alert("error: " + error); }
  };
  useEffect(() => {
      const interval = setInterval(async () => {
        try {
          const response = await fetch("http://localhost:4000/checkBiometric");
          if(!response.ok) throw new Error('HTTP error: ' + response.status);
          const data = await response.json();
          console.log("BiometricVerification: " + data.biometricVerification)
          if(data.biometricVerification) {
            clearInterval(interval);
            navigate('/index');
          }
        } catch(error) { alert("Error: " + error); } 
      }, 3000); //3 sek
    }, []);

    return (
    <div className="App">
      <header className="App-header">
        <div>
          <h1>Wybierz metodę uwierzytelnienia:</h1>
          <button onClick={sendEMail}>Kod e-mail</button>
          <br></br>
          <button onClick={sendSMS}>Kod SMS</button>
          <br></br>
          <button onClick={sendMobileCode}>Kod aplikacji uwierzytelniającej</button>
          <br></br>
          <button onClick={sendQRCodeVer}>Kod QR</button>
          <br></br>
          <button onClick={sendBioNotifi}>Skan odcisku palca</button>
          <br></br>
          <button onClick={handleClick}>Wyloguj</button>
        </div>
      </header>
    </div>
    )
}
export default Login2FA;