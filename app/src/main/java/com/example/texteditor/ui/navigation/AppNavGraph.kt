package com.example.texteditor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.texteditor.ui.editor.EditorScreen
import com.example.texteditor.ui.theme.RichTextEditorTheme
import com.example.texteditor.ui.documentlist.DocumentListScreen

sealed class Screen(val route: String) {
    data object DocumentList : Screen("document_list")
    data object Editor : Screen("editor/{documentId}") {
        fun createRoute(documentId: Long) = "editor/$documentId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.DocumentList.route
    ) {
        composable(Screen.DocumentList.route) {
            DocumentListScreen(
                onDocumentClick = { docId ->
                    navController.navigate(Screen.Editor.createRoute(docId))
                },
                onNewDocument = {
                    navController.navigate(Screen.Editor.createRoute(-1L))
                }
            )
        }
        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) {
            EditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavGraphPreview() {
    RichTextEditorTheme {
        AppNavGraph()
    }
}
