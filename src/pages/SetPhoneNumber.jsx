import { useState } from "react";
import { useNavigate } from "react-router-dom";

const SetPhoneNumber = () => {
  const [phoneNumber, setPhoneNumber] = useState(null);
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    try {
          const sendNumber = await fetch("http://localhost:4000/setPhonenumber", {
              method: "POST",
              headers: {"Content-Type":"application/json"},
              credentials: "include",
              body: JSON.stringify({ phoneNumber}),
            });
            if(!sendNumber.ok) throw new Error ("HTTP Error: " + sendNumber.status);
            navigate('/Set2FA')
        } catch (error) { alert("error: " + error); } 
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

    return (
    <div className="App">
      <header className="App-header">
        <div>
          Dodaj numer telefonu do konta:
          <br></br>
          Format: +11123456789
        </div>
        <form onSubmit={handleSubmit}>
          <input
          type="tel"
          id="phone"
          placeholder="+11123456789"
          pattern="[+]{1}[0-9]{2}[0-9]{3}[0-9]{3}[0-9]{3}"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          required
          />
          <br></br>
          <button type='submit'>Dodaj numer</button>
        </form>
        <br></br>
        <button onClick={handleClick}>Wyloguj</button>
      </header>
    </div>
    );
}

export default SetPhoneNumber;