import './App.css';
import LoginForm from './pages/LoginForm';
import SetPhoneNumber from './pages/SetPhoneNumber';
import Set2FA from './pages/Set2FA';
import { useEffect, useState } from 'react';
import { Route, Routes, Navigate, useNavigate } from 'react-router-dom';
import Index from './pages/index';
import Login2FA from './pages/Login2FA';
import EmailCode from './pages/EmailCode';
import SMSCode from './pages/SMSCode';
import QRCodeVerification from './pages/QRCodeVerification';
import AppCodeVerification from './pages/AppCodeVerification';

function App() {
  const [isLogged, setIsLogged] = useState(null);
  const [hasPhone, setHasPhone] = useState(null);
  const [has2FA, setHas2FA] = useState(null);
  const navigate = useNavigate();

  //pobieranie danych użytkownika
  useEffect(() => {
    const checkUser = async () => {
      try { 
      const res = await fetch("http://localhost:4000/checkUser", {
        credentials: "include",
      });
      const data = await res.json();
      setIsLogged(data.loggedIn);
      setHasPhone(data.hasPhone || false);
      setHas2FA(data.has2FA || false);
      } catch (error) {
      setIsLogged(false);
      }
    };
    checkUser();
  }, []);

  //logowanie oraz navigacja do odpowiedniej storny zaleznie od danych
  const handleLoginSuccess = async () => {
    try {
      const res = await fetch("http://localhost:4000/checkUser", {
        credentials: "include",
      });
      const data = await res.json();
      setIsLogged(data.loggedIn);
      setHasPhone(data.hasPhone || false);
      setHas2FA(data.has2FA || false);

      if (data.loggedIn && !data.hasPhone) {
        navigate('/SetPhoneNumber');
      } else if (data.loggedIn && !data.has2FA) {
        navigate('/Set2FA');
      } else if (data.loggedIn && data.has2FA) {
        navigate('/Login2FA');
      }

    } catch (e) {
      alert("Błąd podczas pobierania danych użytkownika.");
    } 
  }

if (isLogged === null) return null;

//nawigacja po podstronach
return (
    <Routes>
      {!isLogged ? (
        <>
          <Route path="/" element={<LoginForm onLoginSuccess={handleLoginSuccess} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </>
      ) :  (
        <>
          <Route path="/SetPhoneNumber" element={<SetPhoneNumber />} />
          <Route path="/Set2FA" element={<Set2FA />} />
          <Route path="/Login2FA" element={<Login2FA />} />
          <Route path="/EmailCode" element={<EmailCode />} />
          <Route path="/SMSCode" element={<SMSCode />} />
          <Route path="/QRCodeVerification" element={<QRCodeVerification />} />
          <Route path="/AppCodeVerification" element={<AppCodeVerification />} />
          <Route path="/index" element={<Index />} />
          <Route path="/" element= {
            hasPhone
             ? has2FA 
             ? <Navigate to="/Login2FA" />
             : <Navigate to="/Set2FA" />
            : <Navigate to="/SetPhoneNumber" /> 
          }
          />
          <Route path="*" element={<Navigate to="/" />} />
        </>
      )}
    </Routes>
)
}

export default App;