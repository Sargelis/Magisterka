import { useState } from "react";
import { useNavigate } from "react-router-dom";

const SMSCode = () => {
    const [userSMSCode, setUserSMSEmail] = useState(null)
    const navigate = useNavigate();

    async function checkSMSCode (e) {
        e.preventDefault();
        try {
          const response = await fetch("http://localhost:4000/getSMSCode");
          if(!response.ok) throw new Error('HTTP error' + response.status);
          const data = await response.json();
          //alert(data.ServerSMSCode)
          //alert(userSMSCode)
          if(data.ServerSMSCode === userSMSCode) { navigate("/index") }
            else { alert("Podano zły kod") }
        } catch(error) { alert("Error: " + error); }
  };

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

    return(
    <div className="App">
      <header className="App-header">
        <div>
            <p>Wprowadź kod podany w wiadomości SMS: </p>
            <form onSubmit={checkSMSCode}>
              <input
              type="text"
              id="SMSCode"
              value={userSMSCode}
              onChange={(e) => setUserSMSEmail(e.target.value)}
              required
              />
              <br></br>
              <button type="submit">Okej</button>
            </form>
          <br></br>
          <button onClick={handleClick}>Wyloguj</button>
        </div>
      </header>
    </div>
    )
};
export default SMSCode;