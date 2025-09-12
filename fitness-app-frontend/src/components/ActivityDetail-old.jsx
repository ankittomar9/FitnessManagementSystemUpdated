import { format } from 'date-fns';
import {
    AccessTime as TimeIcon,
    Whatshot as CaloriesIcon,
    FitnessCenter as ActivityIcon,
    TrendingUp as IntensityIcon
} from '@mui/icons-material';

import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router";
import { getActivities } from "../services/api";
import {
    Grid,
    Card,
    CardContent,
    Typography,
    Box,
    Chip,
    LinearProgress,
    Paper,
    useTheme,
    useMediaQuery
} from "@mui/material";
import {
    AccessTime as TimeIcon,
    Whatshot as CaloriesIcon,
    FitnessCenter as ActivityIcon,
    TrendingUp as IntensityIcon
} from '@mui/icons-material';
import { format } from 'date-fns';

const ActivityList = () => {
    const [activities, setActivities] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

    const fetchActivities = async () => {
        try {
            setLoading(true);
            const response = await getActivities();
            setActivities(response.data || []);
        } catch (error) {
            console.error('Error fetching activities:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchActivities();
    }, []);

    const getActivityColor = (type) => {
        const colors = {
            RUNNING: '#4caf50',
            CYCLING: '#2196f3',
            SWIMMING: '#00bcd4',
            YOGA: '#9c27b0',
            WEIGHT_TRAINING: '#ff9800',
            HIIT: '#f44336'
        };
        return colors[type] || '#757575';
    };

    if (loading) {
        return <LinearProgress />;
    }

    return (
        <Box sx={{ p: isMobile ? 1 : 3 }}>
            <Typography
                variant="h4"
                gutterBottom
                sx={{
                    mb: 3,
                    fontWeight: 'bold',
                    color: 'primary.main',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1
                }}
            >
                <ActivityIcon /> My Activities
            </Typography>

            {activities.length === 0 ? (
                <Paper
                    elevation={0}
                    sx={{
                        p: 4,
                        textAlign: 'center',
                        backgroundColor: 'background.paper',
                        borderRadius: 2
                    }}
                >
                    <Typography variant="h6" color="textSecondary">
                        No activities recorded yet
                    </Typography>
                    <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
                        Start tracking your fitness journey by adding your first activity!
                    </Typography>
                </Paper>
            ) : (
                <Grid container spacing={3}>
                    {activities.map((activity) => (
                        <Grid item xs={12} sm={6} lg={4} key={activity.id}>
                            <Card
                                elevation={3}
                                onClick={() => navigate(`/activities/${activity.id}`)}
                                sx={{
                                    height: '100%',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    transition: 'all 0.3s ease',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        boxShadow: 6,
                                        cursor: 'pointer'
                                    },
                                    borderLeft: `4px solid ${getActivityColor(activity.type)}`
                                }}
                            >
                                <CardContent sx={{ flexGrow: 1, p: 3 }}>
                                    <Box sx={{
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        alignItems: 'flex-start',
                                        mb: 2
                                    }}>
                                        <Typography
                                            variant="h6"
                                            component="div"
                                            sx={{
                                                fontWeight: 'bold',
                                                textTransform: 'capitalize'
                                            }}
                                        >
                                            {activity.type.replace('_', ' ').toLowerCase()}
                                        </Typography>
                                        <Chip
                                            label={activity.intensity || 'Moderate'}
                                            size="small"
                                            color="primary"
                                            variant="outlined"
                                            icon={<IntensityIcon fontSize="small" />}
                                        />
                                    </Box>

                                    <Box sx={{
                                        display: 'flex',
                                        flexWrap: 'wrap',
                                        gap: 1.5,
                                        mb: 2
                                    }}>
                                        <Chip
                                            icon={<TimeIcon />}
                                            label={`${activity.duration || 0} min`}
                                            variant="outlined"
                                            size="small"
                                        />
                                        <Chip
                                            icon={<CaloriesIcon />}
                                            label={`${activity.caloriesBurned || 0} kcal`}
                                            color="error"
                                            variant="outlined"
                                            size="small"
                                        />
                                    </Box>

                                    {activity.date && (
                                        <Typography
                                            variant="caption"
                                            color="text.secondary"
                                            sx={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                mt: 1
                                            }}
                                        >
                                            {format(new Date(activity.date), 'MMM d, yyyy')}
                                        </Typography>
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            )}
        </Box>
    );
};

export default ActivityList;