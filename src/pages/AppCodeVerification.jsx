import { useNavigate } from "react-router-dom";
import { useState } from "react";

const AppCodeVerification = () => {
  const navigate = useNavigate();
  const [appCode, setAppCode] = useState(null);


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

  async function handleSubmit(e) {
    e.preventDefault();
    try {
        const response = await fetch("http://localhost:4000/verifiNumberCode")
        if(!response.ok) throw new Error('HTTP error: ' + response.status);
        const data = await response.json();
        if(data.code.toString() === appCode?.toString()) navigate("/index");
        else alert("Błędny kod");
    } catch (error) { alert("error: " + error) }
  } 

    return (
    <div className="App">
      <header className="App-header">
        <div>
          Wpisz 6-cyfrowy kod z aplikacji mobilnej
          <br></br>
          <form onSubmit={handleSubmit}>
            <input
            type='number'
            id='number'
            value={appCode}
            onChange={(e) => setAppCode(e.target.value)}
            required
            />
            <br></br>
            <button type='submit'>Okej</button>
          </form>
          <button onClick={handleClick}>Wyloguj</button>
        </div>
      </header>
    </div>
    )
}
export default AppCodeVerification;