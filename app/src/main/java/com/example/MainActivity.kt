package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.model.UserRole
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.components.UserSwitchDialog
import com.example.ui.screens.*
import com.example.ui.theme.SportsNavyDark
import com.example.ui.theme.SportsOpsTheme
import com.example.viewmodel.SportsOpsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Dashboard", Icons.Default.Dashboard)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Assignment)
    object Events : Screen("events", "Events", Icons.Default.EmojiEvents)
    object Issues : Screen("issues", "Issues", Icons.Default.ReportProblem)
    object Calendar : Screen("calendar", "Schedule", Icons.Default.CalendarMonth)
    object Team : Screen("team", "Team", Icons.Default.Groups)
    object Approvals : Screen("approvals", "Approvals", Icons.Default.FactCheck)
    object AiOps : Screen("ai_ops", "AI Ops", Icons.Default.AutoAwesome)

    object TaskDetail : Screen("task_detail/{taskId}", "Task Detail", Icons.Default.Assignment) {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object CreateTask : Screen("create_task", "Create Task", Icons.Default.Add)
    object EventDetail : Screen("event_detail/{eventId}", "Event Detail", Icons.Default.EmojiEvents) {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object ProposalReview : Screen("proposal_review/{eventId}", "Proposal Review", Icons.Default.RateReview) {
        fun createRoute(eventId: String) = "proposal_review/$eventId"
    }
    object IssueDetail : Screen("issue_detail/{issueId}", "Issue Detail", Icons.Default.ReportProblem) {
        fun createRoute(issueId: String) = "issue_detail/$issueId"
    }
    object CreateIssue : Screen("create_issue", "Report Issue", Icons.Default.Add)
}

class MainActivity : ComponentActivity() {
    private val viewModel: SportsOpsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportsOpsTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: SportsOpsViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser by viewModel.currentUser.collectAsState()
    val pendingApprovals by viewModel.approvals.collectAsState()
    val pendingCount = pendingApprovals.count { it.status == com.example.model.CoreApprovalStatus.PENDING }

    var showUserSwitchDialog by remember { mutableStateOf(false) }
    var showGlobalSearchDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Tasks,
        Screen.Events,
        Screen.Issues,
        Screen.Calendar
    )

    val shouldShowBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToTaskDetail = { taskId -> navController.navigate(Screen.TaskDetail.createRoute(taskId)) },
                    onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                    onNavigateToEventDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                    onNavigateToIssues = { navController.navigate(Screen.Issues.route) },
                    onNavigateToIssueDetail = { issueId -> navController.navigate(Screen.IssueDetail.createRoute(issueId)) },
                    onNavigateToApprovals = { navController.navigate(Screen.Approvals.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                    onNavigateToAiOps = { navController.navigate(Screen.AiOps.route) },
                    onOpenRoleSwitcher = { showUserSwitchDialog = true },
                    onOpenGlobalSearch = { showGlobalSearchDialog = true },
                    onOpenCloudSync = { showCloudSyncDialog = true }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen(
                    viewModel = viewModel,
                    onNavigateToTaskDetail = { taskId -> navController.navigate(Screen.TaskDetail.createRoute(taskId)) },
                    onNavigateToCreateTask = { navController.navigate(Screen.CreateTask.route) }
                )
            }

            composable(Screen.Events.route) {
                EventsScreen(
                    viewModel = viewModel,
                    onNavigateToEventDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                    onNavigateToCreateEvent = { /* Can link to create */ }
                )
            }

            composable(Screen.Issues.route) {
                IssuesScreen(
                    viewModel = viewModel,
                    onNavigateToIssueDetail = { issueId -> navController.navigate(Screen.IssueDetail.createRoute(issueId)) },
                    onNavigateToCreateIssue = { navController.navigate(Screen.CreateIssue.route) }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Team.route) {
                TeamScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Approvals.route) {
                ApprovalsScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.AiOps.route) {
                AiOpsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                TaskDetailScreen(
                    taskId = taskId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTaskDetail = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) }
                )
            }

            composable(Screen.CreateTask.route) {
                CreateEditTaskScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTaskDetail = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) },
                    onNavigateToProposalReview = { id -> navController.navigate(Screen.ProposalReview.createRoute(id)) }
                )
            }

            composable(
                route = Screen.ProposalReview.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                ProposalReviewScreen(
                    eventId = eventId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.IssueDetail.route,
                arguments = listOf(navArgument("issueId") { type = NavType.StringType })
            ) { backStackEntry ->
                val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
                IssueDetailScreen(
                    issueId = issueId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateIssue.route) {
                CreateIssueScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Role Switch Dialog
    if (showUserSwitchDialog) {
        UserSwitchDialog(
            viewModel = viewModel,
            onDismiss = { showUserSwitchDialog = false }
        )
    }

    // Universal Global Search Dialog
    if (showGlobalSearchDialog) {
        GlobalSearchDialog(
            viewModel = viewModel,
            onDismiss = { showGlobalSearchDialog = false },
            onNavigateToTaskDetail = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) },
            onNavigateToEventDetail = { id -> navController.navigate(Screen.EventDetail.createRoute(id)) },
            onNavigateToIssueDetail = { id -> navController.navigate(Screen.IssueDetail.createRoute(id)) }
        )
    }

    // Cloud Backend & Firestore Sync Dialog
    if (showCloudSyncDialog) {
        CloudSyncDialog(
            viewModel = viewModel,
            onDismiss = { showCloudSyncDialog = false }
        )
    }
}
