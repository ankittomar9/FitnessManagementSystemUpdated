import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { getActivityDetail } from '../services/api';
import {
    Box,
    Card,
    CardContent,
    Divider,
    Typography,
    Chip,
    LinearProgress,
    Grid,
    Paper,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    useTheme,
    useMediaQuery
} from '@mui/material';
import {
    AccessTime as TimeIcon,
    Whatshot as CaloriesIcon,
    Event as DateIcon,
    TrendingUp as ImprovementIcon,
    Lightbulb as SuggestionIcon,
    Security as SafetyIcon,
    FitnessCenter as ActivityIcon,
    Info as AnalysisIcon
} from '@mui/icons-material';
import { format } from 'date-fns';

const ActivityDetail = () => {
    const { id } = useParams();
    const [activity, setActivity] = useState(null);
    const [loading, setLoading] = useState(true);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

    useEffect(() => {
        const fetchActivityDetail = async () => {
            try {
                setLoading(true);
                const response = await getActivityDetail(id);
                setActivity(response.data);
            } catch (error) {
                console.error('Error fetching activity details:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchActivityDetail();
    }, [id]);

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

    const renderListWithIcons = (items, icon, fallbackText) => {
        if (!items || items.length === 0) {
            return (
                <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                    {fallbackText}
                </Typography>
            );
        }

        return (
            <List dense>
                {items.map((item, index) => (
                    <ListItem key={index} sx={{ px: 0, alignItems: 'flex-start' }}>
                        <ListItemIcon sx={{ minWidth: 36, mt: 0.5 }}>
                            {React.cloneElement(icon, { color: "primary", fontSize: "small" })}
                        </ListItemIcon>
                        <ListItemText primary={item} />
                    </ListItem>
                ))}
            </List>
        );
    };

    if (loading) {
        return <LinearProgress />;
    }

    if (!activity) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <Typography variant="h6" color="textSecondary">
                    Activity not found
                </Typography>
            </Box>
        );
    }

    return (
        <Box sx={{ maxWidth: 1200, mx: 'auto', p: isMobile ? 1 : 3 }}>
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
                <ActivityIcon /> Activity Details
            </Typography>

            <Grid container spacing={3}>
                {/* Activity Summary Card */}
                <Grid item xs={12} md={4}>
                    <Card elevation={3} sx={{ height: '100%' }}>
                        <CardContent>
                            <Box sx={{
                                display: 'flex',
                                alignItems: 'center',
                                mb: 2,
                                p: 2,
                                backgroundColor: 'primary.light',
                                color: 'primary.contrastText',
                                borderRadius: 1
                            }}>
                                <ActivityIcon sx={{ mr: 1 }} />
                                <Typography variant="h5">
                                    {activity.type.replace('_', ' ')}
                                </Typography>
                            </Box>

                            <Box sx={{ p: 2 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <DateIcon color="action" sx={{ mr: 1 }} />
                                    <Typography>
                                        {format(new Date(activity.createdAt), 'PPpp')}
                                    </Typography>
                                </Box>

                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <TimeIcon color="action" sx={{ mr: 1 }} />
                                    <Typography>
                                        Duration: {activity.duration} minutes
                                    </Typography>
                                </Box>

                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    <CaloriesIcon color="error" sx={{ mr: 1 }} />
                                    <Typography>
                                        Calories Burned: {activity.caloriesBurned}
                                    </Typography>
                                </Box>
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                {/* AI Analysis and Recommendations */}
                <Grid item xs={12} md={8}>
                    {activity.recommendation && (
                        <Card elevation={3} sx={{ mb: 3 }}>
                            <CardContent>
                                <Typography
                                    variant="h6"
                                    gutterBottom
                                    sx={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        color: 'primary.main'
                                    }}
                                >
                                    <AnalysisIcon sx={{ mr: 1 }} />
                                    AI Analysis
                                </Typography>
                                <Paper
                                    variant="outlined"
                                    sx={{
                                        p: 2,
                                        bgcolor: 'background.paper',
                                        borderRadius: 1
                                    }}
                                >
                                    <Typography variant="body1">
                                        {activity.recommendation}
                                    </Typography>
                                </Paper>
                            </CardContent>
                        </Card>
                    )}

                    <Grid container spacing={3}>
                        {/* Improvements */}
                        <Grid item xs={12} md={6}>
                            <Card elevation={3} sx={{ height: '100%' }}>
                                <CardContent>
                                    <Typography
                                        variant="h6"
                                        gutterBottom
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            color: 'success.main'
                                        }}
                                    >
                                        <ImprovementIcon sx={{ mr: 1 }} />
                                        Suggested Improvements
                                    </Typography>
                                    {renderListWithIcons(
                                        activity.improvements,
                                        <ImprovementIcon />,
                                        "No improvement suggestions available"
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>

                        {/* Exercise Suggestions */}
                        <Grid item xs={12} md={6}>
                            <Card elevation={3} sx={{ height: '100%' }}>
                                <CardContent>
                                    <Typography
                                        variant="h6"
                                        gutterBottom
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            color: 'info.main'
                                        }}
                                    >
                                        <SuggestionIcon sx={{ mr: 1 }} />
                                        Exercise Suggestions
                                    </Typography>
                                    {renderListWithIcons(
                                        activity.suggestions,
                                        <SuggestionIcon />,
                                        "No exercise suggestions available"
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>

                        {/* Safety Guidelines */}
                        <Grid item xs={12}>
                            <Card elevation={3}>
                                <CardContent>
                                    <Typography
                                        variant="h6"
                                        gutterBottom
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            color: 'warning.main'
                                        }}
                                    >
                                        <SafetyIcon sx={{ mr: 1 }} />
                                        Safety Guidelines
                                    </Typography>
                                    {renderListWithIcons(
                                        activity.safety,
                                        <SafetyIcon />,
                                        "No specific safety guidelines available"
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </Box>
    );
};

export default ActivityDetail;