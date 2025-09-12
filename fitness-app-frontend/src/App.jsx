import {useContext, useEffect, useState} from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import {BrowserRouter as Router,Navigate,Route,Routes,useLocation}  from "react-router";
import {Box, Button} from "@mui/material";
import {AuthContext} from "react-oauth2-code-pkce";
import {useDispatch} from "react-redux";
import {setCredentials} from "./store/authSlice.js";

const ActivtiesPage = () => {
 <Box component ="section" sx={{p:2,border:"1px solid #ccc"}}>
 <ActivityForm onActivtiyAdded={() => window.location.reload}/>
<AcrivtyList/>
</Box>
}


function App() {
const {token,tokenData,logIn,logOut,isAuthenticated}=useContext(AuthContext);
const dispatch=useDispatch();
const [authReady,setAuthReady]=useState(false);
    useEffect(() => {
        if(token){
            dispatch(setCredentials({token,user:tokenData}));
            setAuthReady(true);
        }
    }, [token,tokenData,dispatch]);
  return (
    <Router>
        {!token?(
      <Button variant="contained" color="#dc004e"
              onClick={() => logIn()}> Login </Button>
             ):(
             //<div>
            // <pre>{JSON.stringify(tokenData,null,2)}
            // </pre> // </div>
            <Box component ="section" sx={{p:2,border:"1px solid #ccc"}}>
              <Routes>
                  <Route path="activities" element={<ActivtiesPage/>}/>
                  

              </Routes>
              





            </Box>
            )}
    </Router>
  )
}

export default App
