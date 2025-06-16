
const index = () => {

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
          <h1>Zalogowano</h1>
          <br></br>
          <button onClick={handleClick}>Wyloguj</button>
        </div>
      </header>
    </div>
    )
}
export default index;