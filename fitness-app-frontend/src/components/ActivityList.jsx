import React, {useEffect, useState} from 'react'
import Grid from '@mui/material/Grid';
import {useNavigate} from "react-router";
import {getActivities} from "../services/api.js";
import {Card, CardContent, Typography} from "@mui/material";

const ActivityList = () => {
    const [activities,setActivities]=useState([]);
    const navigate=useNavigate();

    const fetchActivities=async () => {
        try {
            const response=await getActivities();
            setActivities(response.data);
        } catch (error) {
            console.error(error);
        }
    }
    useEffect(() => {
        fetchActivities();
    }, []);

    return (
        <Grid container spacing={2}> {/* Use Grid container */}
            {activities.map((activity) => (
                <Grid item xs={12} sm={6} md={4} key={activity.id}> {/* Use Grid item */}
                    <Card sx={{ cursor: 'pointer' }}
                          onClick={() => navigate(`/activities/${activity.id}`)}>
                        <CardContent>
                            <Typography variant='h6'>{activity.type}</Typography>
                            <Typography>Duration: {activity.duration}</Typography>
                            <Typography>Calories: {activity.caloriesBurned}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
};

export default ActivityList;