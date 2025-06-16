import { useState } from "react";
import { useNavigate } from "react-router-dom";

const EmailCode = () => {
    const [userEmailCode, setUserCodeEmail] = useState(null)
    const navigate = useNavigate();

    async function checkEmailCode (e) {
        e.preventDefault();
        try {
          const response = await fetch("http://localhost:4000/getEmailCode");
          if(!response.ok) throw new Error('HTTP error' + response.status);
          const data = await response.json();
          //alert(data.ServerEmailCode)
          //alert(userEmailCode)
          if(data.ServerEmailCode === userEmailCode) { navigate("/index") }
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
            <p>Wprowadź kod podany w wiadomości Email: </p>
            <form onSubmit={checkEmailCode}>
              <input
              type="text"
              id="EmailCode"
              value={userEmailCode}
              onChange={(e) => setUserCodeEmail(e.target.value)}
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
export default EmailCode;