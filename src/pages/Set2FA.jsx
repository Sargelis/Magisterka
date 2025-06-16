import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";


const Set2FA = () => {
  const navigate = useNavigate();
  const [code, setCode] = useState(null);
  const [qrImage, setQrImage] = useState(null);

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

  const showCode = async () => {
    try {
      const response = await fetch("http://localhost:4000/connCode")
      if(!response.ok) throw new Error ("HTTP Error: " + response.status)
      const data = await response.json();
      setCode(data);
    } catch(error) { alert("error: " + error); }
};

    const showQRCode = async () => {
    try {
      const response = await fetch("http://localhost:4000/qrCode");
      if(!response.ok) throw new Error('HTTP error: ' + response.status);
      const QRCode = await response.json();
      setQrImage(QRCode);
    } catch(error) { alert("Error: " + error); }
};

useEffect(() => {
  const interval = setInterval(async () => {
    try {
      const response = await fetch("http://localhost:4000/check2FA");
      if(!response.ok) throw new Error('HTTP error: ' + response.status);
      const data = await response.json();
      if(data) {
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
          <h1>Dodaj aplikację uwierzytelniającą</h1>
          Wybierz metodę
          <br></br>
          <button onClick={showQRCode}>Kod QR do zeskanowania</button>
          <br></br>
          <button onClick={showCode}>Kod do wpisania w aplikacji</button>
          <br></br>
          {qrImage && (
          <img src={qrImage} width={300} height={300} style={{ border: "none" }} />
          )}
          <br></br>
          <div>{code}</div>
          <br></br>
          <button onClick={handleClick}>Wyloguj</button>
        </div>
      </header>
    </div>
    )
}
export default Set2FA;