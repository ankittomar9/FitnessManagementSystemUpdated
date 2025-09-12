import {useContext, useEffect, useState} from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import {BrowserRouter as Router,Navigate,Route,Routes,useLocation}  from "react-router";
import {Box, Button} from "@mui/material";
import {AuthContext} from "react-oauth2-code-pkce";
import {useDispatch} from "react-redux";
import {setCredentials} from "./store/authSlice.js";
import ActivityDetail from "./components/ActivityDetail.jsx";
import ActivityForm from "./components/ActivityForm.jsx";
import ActivityList from "./components/ActivityList.jsx";

const ActivitiesPage = () => {
  return(  <Box sx={{p:2,border:"1px solid #ccc"}}>
 <ActivityForm onActivitiyAdded={() => window.location.reload}/>
<ActivityList/>
</Box>);
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
              <Button variant="contained" color="secondary" onClick={logOut}>
                  Logout
                </Button>
              <Routes>
                <Route path="/activities" element={<ActivitiesPage />}/>
           
                 <Route path="/activities/:id" element={<ActivityDetail />}/>

                <Route path="/" element={token ? <Navigate to="/activities" replace/> : <div>Welcome! Please Login.</div>} />
              </Routes>
            </Box>
            )}
    </Router>
  )
}

export default App
