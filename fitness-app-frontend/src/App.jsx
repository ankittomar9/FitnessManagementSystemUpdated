import {useContext, useEffect, useState} from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import {BrowserRouter as Router,Navigate,Route,Routes,useLocation}  from "react-router";
import {Button} from "@mui/material";
import {AuthContext} from "react-oauth2-code-pkce";
import {useDispatch} from "react-redux";
import {setCredentials} from "./store/authSlice.js";

function App() {
const {token,tokenData,logIn,logOut,isAuthenticated}=useContext(AuthContext);
const dispatch=useDispatch();
const [authReady,setAuthReady]=useState(false);
    useEffect(() => {
        if(token){
            dispatch(setCredentials({token,tokenData}));
            setAuthReady(true);
        }
    }, [token,tokenData,dispatch]);
  return (
    <Router>
        {!token?(
      <Button variant="contained" color="#dc004e"
              onClick={() => logIn()}> Login </Button>
            ):(<div>
            <pre>{JSON.stringify(tokenData,null,2)}
            </pre>
            </div>
            )}
    </Router>
  )
}

export default App
