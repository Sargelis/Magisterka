import { useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";

const QRCodeVerification = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const qrImage = location.state || {};


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

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const response = await fetch("http://localhost:4000/checkQR");
        if(!response.ok) throw new Error('HTTP error: ' + response.status);
        const data = await response.json();
        console.log(data.checkQR)
        if(data.checkQR) {
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
          Zeskanuj za pomocą aplikacji uwierzytelniającej
          <br></br>
          {qrImage && (
          <img src={qrImage} width={300} height={300} style={{ border: "none" }} />
          )}
          <br></br>
          <button onClick={handleClick}>Wyloguj</button>
        </div>
      </header>
    </div>
    )
}
export default QRCodeVerification;