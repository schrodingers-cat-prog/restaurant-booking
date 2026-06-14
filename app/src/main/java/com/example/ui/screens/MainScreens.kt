package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontStyle
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.viewmodel.RestaurantViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.example.ui.theme.ThemeState

// Style Tokens: Terracotta Warm Palette
val GoldPrimary: Color
    @Composable
    get() = ThemeState.currentPrimaryColor

val AmberSecondary = Color(0xFFFFB300)
val SandBackground = Color(0xFFFCF9F2)
val DarkText = Color(0xFF2E241E)
val CardBackground = Color(0xFFFFFFFF)
val MutedSlate = Color(0xFF7D7068)
val SuccessGreen = Color(0xFF2E7D32)
val LightSage = Color(0xFFE8F5E9)

// Helper to launch WhatsApp safely
fun launchWhatsApp(context: Context, text: String, phone: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=${android.net.Uri.encode(text)}"
            )
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp is not installed. Sending copy to clipboard.", Toast.LENGTH_SHORT).show()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Ichiraku Ramen Order", text)
        clipboard.setPrimaryClip(clip)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: RestaurantViewModel) {
    val branches by viewModel.branches.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val isReady by viewModel.uiStateReady.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Branches, 1 = Menu/Ordering, 2 = Orders & Reservations
    var currentBranchDetail by remember { mutableStateOf<Branch?>(null) }
    var activeBookingBranch by remember { mutableStateOf<Branch?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var lastReceiptOrder by remember { mutableStateOf<Order?>(null) }
    var lastReceiptBooking by remember { mutableStateOf<Booking?>(null) }

    if (!isReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SandBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = GoldPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Welcoming deliciousness inside...",
                    color = DarkText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_main_scaffold"),
        containerColor = SandBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(GoldPrimary.copy(alpha = 0.9f), GoldPrimary)
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Ichiraku Ramen icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ICHIRAKU RAMEN",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Text(
                            "Authentic Ninja Ramen Hub & Broth",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                    }

                    // Cart & Settings Header Actions Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val cartItemCount = cart.values.sum()
                        if (cartItemCount > 0) {
                            IconButton(
                                onClick = { showCartDialog = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .testTag("toolbar_cart_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = AmberSecondary,
                                            contentColor = DarkText
                                        ) {
                                            Text("$cartItemCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .testTag("toolbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("app_bottom_nav")
                    .navigationBarsPadding(),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 0) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                            contentDescription = "Branches"
                        )
                    },
                    label = { Text("Branches") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_branches_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 1) Icons.Filled.RestaurantMenu else Icons.Outlined.RestaurantMenu,
                            contentDescription = "Order"
                        )
                    },
                    label = { Text("Order Food") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_menu_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 2) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_history_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Dispatcher based on chosen bottom navigation tab
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "tab_fade"
            ) { targetTab ->
                when (targetTab) {
                    0 -> BranchesScreen(
                        viewModel = viewModel,
                        onViewDetails = { branch -> currentBranchDetail = branch },
                        onBookTable = { branch -> activeBookingBranch = branch },
                        onOrderOnline = { branch ->
                            viewModel.selectBranch(branch.id)
                            activeTab = 1
                        }
                    )
                    1 -> MenuScreen(
                        viewModel = viewModel,
                        onOpenCart = { showCartDialog = true }
                    )
                    2 -> HistoryScreen(
                        viewModel = viewModel,
                        onResendOrderWhatsApp = { order ->
                            viewModel.selectBranch(order.branchId)
                            val phone = order.customerPhone.ifEmpty { "+91 78900 20002" }
                            val text = viewModel.getWhatsAppTextForOrder(order, phone)
                            launchWhatsApp(viewModel.getApplication(), text, phone)
                        },
                        onResendBookingWhatsApp = { booking ->
                            viewModel.selectBranch(booking.branchId)
                            val phone = booking.customerPhone.ifEmpty { "+91 78900 20002" }
                            val text = viewModel.getWhatsAppTextForBooking(booking, phone)
                            launchWhatsApp(viewModel.getApplication(), text, phone)
                        }
                    )
                }
            }

            // Cart Alert Strip overlay on order screen
            val cartItemCount = cart.values.sum()
            val totalPrice = cart.entries.sumOf { it.key.price * it.value }
            if (activeTab == 1 && cartItemCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { showCartDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .testTag("view_cart_strip_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingBag, "cart icon", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "$cartItemCount ${if (cartItemCount == 1) "item" else "items"} added",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "₹${String.format(Locale.US, "%.0f", totalPrice)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, "go to checkout", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Auxiliary Dialogs ---

    // 1. Branch Detail & Review System Dialog
    currentBranchDetail?.let { branch ->
        BranchDetailDialog(
            branch = branch,
            viewModel = viewModel,
            onDismiss = { currentBranchDetail = null },
            onBookTable = {
                currentBranchDetail = null
                activeBookingBranch = branch
            }
        )
    }

    // 2. Table Booking Creation Sheet
    activeBookingBranch?.let { branch ->
        TableBookingDialog(
            branch = branch,
            viewModel = viewModel,
            onDismiss = { activeBookingBranch = null },
            onBookingConfirmed = { booking ->
                activeBookingBranch = null
                lastReceiptBooking = booking
                playAndroidChime(viewModel, true)
                // Automatically open WhatsApp with booking confirmation to customer
                val text = viewModel.getWhatsAppTextForBooking(booking, booking.customerPhone)
                launchWhatsApp(viewModel.getApplication(), text, booking.customerPhone)
            }
        )
    }

    // 3. Checkout Screen Dialog
    if (showCartDialog) {
        CartCheckoutDialog(
            viewModel = viewModel,
            onDismiss = { showCartDialog = false },
            onOrderPlaced = { order ->
                showCartDialog = false
                lastReceiptOrder = order
                playAndroidChime(viewModel, true)
                // Trigger WhatsApp transition immediately!
                val text = viewModel.getWhatsAppTextForOrder(order, order.customerPhone)
                launchWhatsApp(viewModel.getApplication(), text, order.customerPhone)
            }
        )
    }

    // 4. Booking Receipt Popup Dialog
    lastReceiptBooking?.let { booking ->
        ReceiptPopup(
            title = "Reservation Confirmed! 📅",
            message = "Your table reservation is confirmed. A message has been prepared for WhatsApp confirmation to your number: ${booking.customerPhone}.",
            actionText = "Send Confirmation on WhatsApp Again",
            onAction = {
                val text = viewModel.getWhatsAppTextForBooking(booking, booking.customerPhone)
                launchWhatsApp(viewModel.getApplication(), text, booking.customerPhone)
            },
            onDismiss = { lastReceiptBooking = null }
        )
    }

    // 5. Order Receipt Popup Dialog
    lastReceiptOrder?.let { order ->
        ReceiptPopup(
            title = "Order Confirmed! 🍜",
            message = "Tasty food is cooking! Your order at ${order.branchName} is confirmed. A confirmation receipt has been prepared for WhatsApp to your number: ${order.customerPhone}.",
            actionText = "Send Invoice on WhatsApp",
            onAction = {
                val text = viewModel.getWhatsAppTextForOrder(order, order.customerPhone)
                launchWhatsApp(viewModel.getApplication(), text, order.customerPhone)
            },
            onDismiss = { lastReceiptOrder = null }
        )
    }

    // 6. Preferences & Settings Configuration Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

// --- Screen 1: Locations Board with reviews ---
@Composable
fun BranchesScreen(
    viewModel: RestaurantViewModel,
    onViewDetails: (Branch) -> Unit,
    onBookTable: (Branch) -> Unit,
    onOrderOnline: (Branch) -> Unit
) {
    val branches by viewModel.branches.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredBranches = remember(branches, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            branches
        } else {
            branches.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Welcome and Header Area
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Savor the Wills of Fire & Broth",
                color = DarkText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Select one of our premium branches below to book reservations, browse localized culinary items, or read curated customer logs.",
                color = MutedSlate,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Dynamic Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by branch name or area...") },
                leadingIcon = { Icon(Icons.Default.Search, "search icon", tint = GoldPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("branch_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = MutedSlate.copy(alpha = 0.4f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredBranches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No branches",
                        tint = MutedSlate.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No branches found matching your search.",
                        color = MutedSlate,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("branches_list"),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredBranches) { branch ->
                    // Calculate dynamic average rating out of user reviews + base reviews
                    val branchReviews = reviews.filter { it.branchId == branch.id }
                    val avgRating = if (branchReviews.isNotEmpty()) {
                        branchReviews.map { it.rating }.average()
                    } else {
                        4.8 // Default high rating for Ichiraku Ramen brand
                    }
                    val reviewCount = branchReviews.size

                    BranchItemCard(
                        branch = branch,
                        avgRating = avgRating,
                        reviewCount = reviewCount,
                        onViewDetails = { onViewDetails(branch) },
                        onBookTable = { onBookTable(branch) },
                        onOrderOnline = { onOrderOnline(branch) }
                    )
                }
            }
        }
    }
}

@Composable
fun BranchItemCard(
    branch: Branch,
    avgRating: Double,
    reviewCount: Int,
    onViewDetails: () -> Unit,
    onBookTable: () -> Unit,
    onOrderOnline: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onViewDetails() }
            .testTag("branch_card_${branch.id}")
    ) {
        Column {
            // Visual decorative container representing each theme uniquely
            val gradientColors = when (branch.imageLabel) {
                "heritage" -> listOf(Color(0xFFE65100), Color(0xFFFFB300))
                "bistro" -> listOf(Color(0xFF6A1B9A), Color(0xFFAD1457))
                "coastal" -> listOf(Color(0xFF006064), Color(0xFF0097A7))
                else -> listOf(Color(0xFF2E7D32), Color(0xFF81C784))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "hours",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            branch.openingHours,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Star badge representing rating
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "rating",
                        tint = AmberSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        String.format(Locale.US, "%.1f", avgRating),
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Body Area
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    branch.name,
                    color = DarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "address icon",
                        tint = GoldPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        branch.address,
                        color = MutedSlate,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    branch.description,
                    color = DarkText.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                Divider(color = SandBackground, thickness = 1.dp)

                // Actions toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        border = BorderStroke(1.dp, MutedSlate.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkText),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.RateReview, "Review details", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reviews ($reviewCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onBookTable,
                        border = BorderStroke(1.dp, GoldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.CalendarToday, "book a table", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Book Table", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOrderOnline,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.MenuBook, "Menu", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Order Food", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Screen 2: Food Menu list with category filter ---
@Composable
fun MenuScreen(
    viewModel: RestaurantViewModel,
    onOpenCart: () -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val cart by viewModel.cart.collectAsState()

    var activeCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Appetizers", "Mains", "Breads", "Desserts", "Drinks")

    val selectedBranch = branches.find { it.id == selectedBranchId }

    val filteredItems = remember(menuItems, activeCategory) {
        if (activeCategory == "All") {
            menuItems
        } else {
            menuItems.filter { it.category.equals(activeCategory, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection indicator of Branch
        Card(
            colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, "branch icon", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ordering from: ${selectedBranch?.name ?: "All Branches (Select below)"}",
                            color = DarkText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedBranch?.address ?: "Select a preferred location from physical branches list.",
                            color = MutedSlate,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Outlined menu select trigger button
                var showBranchPicker by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { showBranchPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.15f), contentColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    ) {
                        Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, "down", modifier = Modifier.size(14.dp))
                    }

                    // Simple branch quickpicker dropdown menu
                    DropdownMenu(
                        expanded = showBranchPicker,
                        onDismissRequest = { showBranchPicker = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        branches.forEach { br ->
                            DropdownMenuItem(
                                text = { Text(br.name) },
                                onClick = {
                                    viewModel.selectBranch(br.id)
                                    showBranchPicker = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Category Row Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("menu_categories_row"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = activeCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { activeCategory = category },
                    label = { Text(category, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = DarkText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) Color.Transparent else MutedSlate.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent,
                        selectedBorderWidth = 0.dp,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Food Items list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("menu_items_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
        ) {
            items(filteredItems) { item ->
                val qtyInCart = cart[item] ?: 0
                MenuItemRowCard(
                    item = item,
                    qtyInCart = qtyInCart,
                    onAdd = { viewModel.addToCart(item) },
                    onRemove = { viewModel.removeFromCart(item) }
                )
            }
        }
    }
}

@Composable
fun MenuItemRowCard(
    item: MenuItem,
    qtyInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag("menu_item_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food category avatar indicator (e.g., Green veg dot vs other)
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(GoldPrimary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.category) {
                        "Appetizers" -> Icons.Default.RestaurantMenu
                        "Mains" -> Icons.Default.DinnerDining
                        "Breads" -> Icons.Default.BakeryDining
                        "Desserts" -> Icons.Default.Icecream
                        else -> Icons.Default.LocalCafe
                    },
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text and price
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Veg indicator
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                            .border(BorderStroke(1.dp, SuccessGreen), RoundedCornerShape(2.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SuccessGreen, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        item.name,
                        color = DarkText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    item.description,
                    color = MutedSlate,
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                )

                Text(
                    "₹${String.format(Locale.US, "%.0f", item.price)}",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Add/Remove Cart Control widget
            if (qtyInCart > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("item_remove_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "decrease quantity",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        "$qtyInCart",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("item_qty_label_${item.id}")
                    )
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .background(GoldPrimary, CircleShape)
                            .testTag("item_add_more_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "increase quantity",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        .testTag("item_add_btn_${item.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "add", modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- Screen 3: History & Saved Bookings Screen ---
@Composable
fun HistoryScreen(
    viewModel: RestaurantViewModel,
    onResendOrderWhatsApp: (Order) -> Unit,
    onResendBookingWhatsApp: (Booking) -> Unit
) {
    val bookings by viewModel.bookings.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Reservations, 1 = Orders History

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers using segment buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { activeSubTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == 0) GoldPrimary else Color.Transparent,
                    contentColor = if (activeSubTab == 0) Color.White else DarkText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                elevation = if (activeSubTab == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
            ) {
                Icon(Icons.Outlined.BookOnline, "booking icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reservations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { activeSubTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == 1) GoldPrimary else Color.Transparent,
                    contentColor = if (activeSubTab == 1) Color.White else DarkText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                elevation = if (activeSubTab == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
            ) {
                Icon(Icons.Outlined.ReceiptLong, "invoice icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Food Orders", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (activeSubTab == 0) {
            // Reservations logs list
            if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = MutedSlate.copy(alpha = 0.5f), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No table reservations found.",
                            color = DarkText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Book a premium dining layout experience at any of our fine branches to begin tracking your reservations here.",
                            color = MutedSlate,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("history_bookings_list"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(bookings) { booking ->
                        BookingHistoryCard(booking = booking, onNotifyWhatsApp = { onResendBookingWhatsApp(booking) })
                    }
                }
            }
        } else {
            // Orders history list
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Outlined.Restaurant, null, tint = MutedSlate.copy(alpha = 0.5f), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No past orders placed yet.",
                            color = DarkText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Browse our rich spicy culinary menu offerings, choose delivery method, and trigger WhatsApp ordering logs.",
                            color = MutedSlate,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("history_orders_list"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(orders) { order ->
                        OrderHistoryCard(
                            order = order,
                            onNotifyWhatsApp = { onResendOrderWhatsApp(order) },
                            onAdvanceStatus = { viewModel.advanceOrderStatus(order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: Booking, onNotifyWhatsApp: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(booking.branchName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                    Text("Ref Code: #CH-${booking.id + 1000}", color = MutedSlate, fontSize = 11.sp)
                }

                // Booking verification state badge
                Box(
                    modifier = Modifier
                        .background(LightSage, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Active Booking", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date and time grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarToday, null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(booking.date, color = DarkText, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.AccessTime, null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(booking.time, color = DarkText, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                    Icon(Icons.Default.People, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${booking.guestsCount} Guests", color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (booking.specialRequests.trim().isNotEmpty()) {
                Text(
                    "Special Request: \"${booking.specialRequests}\"",
                    color = MutedSlate,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = SandBackground, thickness = 1.dp)

            // Resend action button
            Button(
                onClick = onNotifyWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(Icons.Default.Share, "whatsapp", modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Resend WhatsApp Confirmation", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
fun StatusDotLabel(label: String, isActiveOrDone: Boolean, isCurrent: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (isCurrent) GoldPrimary else if (isActiveOrDone) SuccessGreen else MutedSlate.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) GoldPrimary else if (isActiveOrDone) DarkText else MutedSlate
        )
    }
}

@Composable
fun OrderHistoryCard(
    order: Order,
    onNotifyWhatsApp: () -> Unit,
    onAdvanceStatus: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(order.branchName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("ID: #CO-${order.id.toString().takeLast(4)}", color = MutedSlate, fontSize = 11.sp)
                        
                        // Status Badge
                        val (statusText, statusBg, statusTextCol) = when (order.status) {
                            "Preparing" -> Triple("Preparing 🍳", Color(0xFFE3F2FD), Color(0xFF1976D2))
                            "Ready" -> Triple("Ready 🍜", Color(0xFFFFF8E1), Color(0xFFF57C00))
                            "Served" -> Triple("Served ✅", Color(0xFFE8F5E9), Color(0xFF388E3C))
                            else -> Triple("Placed 📦", Color(0xFFFFF3E0), Color(0xFFE65100))
                        }
                        Box(
                            modifier = Modifier
                                .background(statusBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = statusTextCol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Text(
                    "₹${String.format(Locale.US, "%.0f", order.totalAmount)}",
                    color = GoldPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Delivery label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (order.deliveryType) {
                        "Dine-In" -> Icons.Default.DinnerDining
                        "Takeaway" -> Icons.Default.Luggage
                        else -> Icons.Default.DeliveryDining
                    },
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${order.deliveryType} Request",
                    color = DarkText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // List of items Ordered
            Text(
                order.orderItemsText,
                color = MutedSlate,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mini visual progress track
            val currentStatus = order.status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SandBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusDotLabel("Placed", true, currentStatus == "Placed")
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(MutedSlate.copy(alpha = 0.3f)))
                StatusDotLabel("Preparing", currentStatus == "Preparing" || currentStatus == "Ready" || currentStatus == "Served", currentStatus == "Preparing")
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(MutedSlate.copy(alpha = 0.3f)))
                StatusDotLabel("Ready", currentStatus == "Ready" || currentStatus == "Served", currentStatus == "Ready")
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(MutedSlate.copy(alpha = 0.3f)))
                StatusDotLabel("Served", currentStatus == "Served", currentStatus == "Served")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = SandBackground, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Invoice & Transition actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Invoice button
                Button(
                    onClick = onNotifyWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Receipt, "invoice", modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ticket", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Advance status button
                if (order.status != "Served") {
                    val nextLabel = when (order.status) {
                        "Placed" -> "Prep Bowl 🍳"
                        "Preparing" -> "Mark Ready 🍜"
                        "Ready" -> "Serve ✅"
                        else -> "Serve ✅"
                    }
                    val btnBg = when (order.status) {
                        "Placed" -> Color(0xFFE65100)
                        "Preparing" -> Color(0xFF1976D2)
                        "Ready" -> Color(0xFFFFB300)
                        else -> GoldPrimary
                    }
                    Button(
                        onClick = onAdvanceStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp)
                    ) {
                        Text(nextLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp)
                            .border(BorderStroke(1.dp, Color(0xFF388E3C).copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Served 🍜", color = Color(0xFF388E3C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- Dialog implementation: Branch detail & location reviews ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchDetailDialog(
    branch: Branch,
    viewModel: RestaurantViewModel,
    onDismiss: () -> Unit,
    onBookTable: () -> Unit
) {
    val reviews by viewModel.reviews.collectAsState()
    val branchReviews = reviews.filter { it.branchId == branch.id }

    var writeReviewMode by remember { mutableStateOf(false) }

    // User review fields
    var reviewerName by remember { mutableStateOf("") }
    var reviewerRating by remember { mutableStateOf(5) }
    var reviewerComment by remember { mutableStateOf("") }

    val computedRating = if (branchReviews.isNotEmpty()) {
        branchReviews.map { it.rating }.average()
    } else {
        4.5
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SandBackground),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .testTag("branch_detail_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Area
                val gradientColors = when (branch.imageLabel) {
                    "heritage" -> listOf(Color(0xFFE65100), Color(0xFFFFB300))
                    "bistro" -> listOf(Color(0xFF6A1B9A), Color(0xFFAD1457))
                    "coastal" -> listOf(Color(0xFF006064), Color(0xFF0097A7))
                    else -> listOf(Color(0xFF2E7D32), Color(0xFF81C784))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(gradientColors))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                branch.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Operating Hours: ${branch.openingHours}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, "close dialog", tint = Color.White)
                        }
                    }
                }

                // Body content: scrolling details & review systems
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("branch_detail_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Location info block
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.LocationOn, "pin", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(branch.address, color = DarkText, fontSize = 13.sp, lineHeight = 18.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, "phone", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Contact Phone: ${branch.phone}", color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    item {
                        // Extensive description
                        Text(
                            "Concept & Experience",
                            color = DarkText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            branch.description,
                            color = MutedSlate,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Booking quick actions trigger
                    item {
                        Button(
                            onClick = onBookTable,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.TableBar, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book Table Reservation Online", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    // Reviews header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Customer Reviews",
                                    color = DarkText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(Icons.Default.Star, "rating star", tint = AmberSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${String.format(Locale.US, "%.1f", computedRating)} Out of 5.0 (${branchReviews.size} logs)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText
                                    )
                                }
                            }

                            Button(
                                onClick = { writeReviewMode = !writeReviewMode },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (writeReviewMode) MutedSlate else AmberSecondary,
                                    contentColor = if (writeReviewMode) Color.White else DarkText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                                    .testTag("branch_write_review_toggle_btn")
                            ) {
                                Icon(Icons.Default.RateReview, "write reviews", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (writeReviewMode) "Cancel" else "Add Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Star review input writing portal
                    if (writeReviewMode) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().testTag("review_entry_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Enhance our Dining Experience",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DarkText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Reviewer Name Inputs
                                    OutlinedTextField(
                                        value = reviewerName,
                                        onValueChange = { reviewerName = it },
                                        placeholder = { Text("Your full name...") },
                                        label = { Text("Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("reviewer_name_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Interactive Star rating selectors
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Rating:  ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        (1..5).forEach { star ->
                                            val isSelected = star <= reviewerRating
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.Star,
                                                contentDescription = "$star stars",
                                                tint = if (isSelected) AmberSecondary else MutedSlate.copy(alpha = 0.3f),
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clickable { reviewerRating = star }
                                                    .padding(2.dp)
                                                    .testTag("rating_star_$star")
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Comment input
                                    OutlinedTextField(
                                        value = reviewerComment,
                                        onValueChange = { reviewerComment = it },
                                        placeholder = { Text("Describe details of flavor, staff courtesy, spacing...") },
                                        label = { Text("Comments Feedback") },
                                        minLines = 3,
                                        maxLines = 5,
                                        modifier = Modifier.fillMaxWidth().testTag("reviewer_comment_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Clear Submission Button
                                    Button(
                                        onClick = {
                                            if (reviewerName.trim().isEmpty() || reviewerComment.trim().isEmpty()) {
                                                Toast.makeText(viewModel.getApplication(), "Please fill in all blanks.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.addReview(
                                                branchId = branch.id,
                                                customerName = reviewerName.trim(),
                                                rating = reviewerRating,
                                                comment = reviewerComment.trim()
                                            )
                                            // Reset parameters
                                            reviewerName = ""
                                            reviewerComment = ""
                                            reviewerRating = 5
                                            writeReviewMode = false
                                            Toast.makeText(viewModel.getApplication(), "Review added! Thank you for the support.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                        modifier = Modifier.fillMaxWidth().testTag("submit_review_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Submit Review Log", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Scrolling Review list cells
                    if (branchReviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No comments submitted yet. Be the first to add review!",
                                    color = MutedSlate,
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(branchReviews) { rev ->
                            ReviewListRowCell(review = rev)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewListRowCell(review: Review) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(GoldPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.customerName.take(1).uppercase(),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        review.customerName,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 13.sp
                    )
                }

                // Render micro stars rating
                Row {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (star <= review.rating) AmberSecondary else MutedSlate.copy(alpha = 0.2f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                review.comment,
                color = DarkText.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Optional parsed timestamp
            val dateText = remember(review.timestamp) {
                val format = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
                format.format(Date(review.timestamp))
            }
            Text(
                dateText,
                color = MutedSlate,
                fontSize = 9.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End
            )
        }
    }
}


// --- Dialog implementation: Booking Creation Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableBookingDialog(
    branch: Branch,
    viewModel: RestaurantViewModel,
    onDismiss: () -> Unit,
    onBookingConfirmed: (Booking) -> Unit
) {
    val defaultPartySize by viewModel.defaultPartySize.collectAsState()
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var guestsNum by remember(defaultPartySize) { mutableStateOf(defaultPartySize.toString()) }
    var reqDate by remember { mutableStateOf("") }
    var reqTime by remember { mutableStateOf("") }
    var specialReq by remember { mutableStateOf("") }

    // Native Branch Table seating layout configs
    val (branchTables, branchThemedTitle) = remember(branch.id) {
        when (branch.id) {
            1 -> Pair(
                listOf(
                    Triple("T1", "Hokage Table 1", 6),
                    Triple("T2", "Team 7 Table 2", 4),
                    Triple("T3", "Academy Counter 3", 2),
                    Triple("T4", "Uchiha Booth 4", 4),
                    Triple("T5", "Hyuga Garden 5", 8),
                    Triple("T6", "Ichiraku Bar 6", 2),
                    Triple("T7", "Ichiraku Bar 7", 2)
                ),
                "🍁 Traditional Ramen Sanctuary Floor"
            )
            2 -> Pair(
                listOf(
                    Triple("S1", "Oasis Pavilion S1", 8),
                    Triple("S2", "Kazekage Canopy S2", 2),
                    Triple("S3", "Desert Rose S3", 4),
                    Triple("S4", "Sandstorm Lounge S4", 6),
                    Triple("S5", "Sun-Patio S5", 4)
                ),
                "🌵 Cozy Sandstorm Wind Shelter"
            )
            else -> Pair(
                listOf(
                    Triple("M1", "Water Cabin M1", 2),
                    Triple("M2", "Deep Steam M2", 4),
                    Triple("M3", "Water's Edge M3", 6),
                    Triple("M4", "Plump Prawn M4", 4),
                    Triple("M5", "Lake Pavilion M5", 12)
                ),
                "🌊 Waterfront Mist Floating Docks"
            )
        }
    }

    var selectedTableId by remember { mutableStateOf("") }
    var selectedTableName by remember { mutableStateOf("") }
    var selectedTableSeats by remember { mutableStateOf<Int?>(null) }

    // Use current date as placeholder
    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        reqDate = format.format(Date())
        reqTime = "19:00"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SandBackground),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .testTag("table_booking_sheet")
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Reserve a Table", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(branch.name, color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "close booking")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = SandBackground, thickness = 1.dp)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    item {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            placeholder = { Text("E.g. Abhay Sharma") },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("booking_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            placeholder = { Text("E.g. +91 99999 99999") },
                            label = { Text("WhatsApp Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("booking_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = reqDate,
                                    onValueChange = { reqDate = it },
                                label = { Text("Reservation Date") },
                                placeholder = { Text("DD/MM/YYYY") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("booking_date_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = reqTime,
                                onValueChange = { reqTime = it },
                                label = { Text("Time Slot") },
                                placeholder = { Text("E.g. 19:30") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("booking_time_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SandBackground, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Total Dining Guests:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkText)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                (1..6).forEach { num ->
                                    val isSelected = guestsNum == "$num"
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) GoldPrimary else Color.White)
                                            .clickable { guestsNum = "$num" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$num",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else DarkText,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }
                    }

                    // Native bird's-eye view table list choosing widget
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SandBackground.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .border(1.dp, SandBackground, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Select Table (${branchThemedTitle})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive Horizontal table choosing row
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(branchTables.size) { idx ->
                                    val (tid, tname, seats) = branchTables[idx]
                                    val isCurrentlySelected = selectedTableId == tid
                                    val tooManyGuests = (guestsNum.toIntOrNull() ?: 2) > seats

                                    Card(
                                        onClick = {
                                            selectedTableId = tid
                                            selectedTableName = tname
                                            selectedTableSeats = seats
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrentlySelected) GoldPrimary else Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isCurrentlySelected) GoldPrimary.copy(alpha = 0.2f) else MutedSlate.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier.height(54.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = tid,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isCurrentlySelected) Color.White else DarkText
                                            )
                                            Text(
                                                text = "Max $seats Pax",
                                                fontSize = 8.sp,
                                                color = if (isCurrentlySelected) Color.White.copy(alpha = 0.8f) else if (tooManyGuests) Color.Red else MutedSlate
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedTableId.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Selected: $selectedTableName (Max: $selectedTableSeats guests)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    modifier = Modifier.align(Alignment.End)
                                )

                                val guests = guestsNum.toIntOrNull() ?: 2
                                selectedTableSeats?.let { max ->
                                    if (guests > max) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "⚠️ Warning: Selected guests ($guests) exceeds table limit ($max)!",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please touch a table chip to allocate your seat.",
                                    fontSize = 9.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = specialReq,
                            onValueChange = { specialReq = it },
                            placeholder = { Text("Allergies, high chair, candlelight, window seat...") },
                            label = { Text("Special Cooking / Layout Request") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("booking_request_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (customerName.trim().isEmpty() || customerPhone.trim().isEmpty() || reqDate.trim().isEmpty() || reqTime.trim().isEmpty()) {
                            Toast.makeText(viewModel.getApplication(), "Please configure details to place booking.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedTableId.isEmpty()) {
                            Toast.makeText(viewModel.getApplication(), "Please select a specific table for fine-dining on the seat chips.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val guests = guestsNum.toIntOrNull() ?: 2
                        selectedTableSeats?.let { maxSeats ->
                            if (guests > maxSeats) {
                                Toast.makeText(viewModel.getApplication(), "$selectedTableName only accommodates up to $maxSeats guests.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }
                        viewModel.makeBooking(
                            branchId = branch.id,
                            branchName = branch.name,
                            name = customerName.trim(),
                            phone = customerPhone.trim(),
                            date = reqDate.trim(),
                            time = reqTime.trim(),
                            guests = guests,
                            specialRequests = specialReq.trim(),
                            tableId = selectedTableId,
                            tableName = selectedTableName,
                            tableSeats = selectedTableSeats,
                            onSuccess = { booking ->
                                onBookingConfirmed(booking)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("booking_confirm_button")
                ) {
                    Icon(Icons.Filled.Check, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm Booking & Open WhatsApp", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}


// --- Dialog implementation: Shopping Cart and Checkout Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutDialog(
    viewModel: RestaurantViewModel,
    onDismiss: () -> Unit,
    onOrderPlaced: (Order) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()

    val currentBranch = branches.find { it.id == selectedBranchId } ?: branches.firstOrNull()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryType by remember { mutableStateOf("Dine-In") } // Dine-In, Takeaway, Delivery

    val totalPrice = cart.entries.sumOf { it.key.price * it.value }
    val taxPrice = totalPrice * 0.05 // 5% service tax
    val grandTotal = totalPrice + taxPrice

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SandBackground),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .testTag("checkout_dialog")
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("My Cart Checkout", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Pick location: ${currentBranch?.name}", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "close cart")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SandBackground, thickness = 1.dp)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Itemized elements summary
                    item {
                        Text("Itemized Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkText)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(cart.entries.toList()) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SandBackground, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.key.name, color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("₹${entry.key.price} x ${entry.value}", color = MutedSlate, fontSize = 11.sp)
                            }
                            Text(
                                "₹${String.format(Locale.US, "%.0f", entry.key.price * entry.value)}",
                                color = DarkText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = SandBackground, thickness = 1.dp)
                    }

                    // Contact fields
                    item {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            placeholder = { Text("Your full name...") },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("checkout_name_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            placeholder = { Text("Your WhatsApp phone index...") },
                            label = { Text("WhatsApp Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    item {
                        // Segmented Delivery Options
                        Text("Delivery Preference:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkText)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SandBackground, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Dine-In", "Takeaway", "Delivery").forEach { type ->
                                val isSelected = deliveryType == type
                                Button(
                                    onClick = { deliveryType = type },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) GoldPrimary else Color.Transparent,
                                        contentColor = if (isSelected) Color.White else DarkText
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(type, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = SandBackground, thickness = 1.dp)
                    }

                    // Numeric aggregates
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GoldPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Subtotal:", color = MutedSlate, fontSize = 12.sp)
                                Text("₹${String.format(Locale.US, "%.2f", totalPrice)}", color = DarkText, fontSize = 12.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Service Tax (5%):", color = MutedSlate, fontSize = 12.sp)
                                Text("₹${String.format(Locale.US, "%.2f", taxPrice)}", color = DarkText, fontSize = 12.sp)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = SandBackground, thickness = 1.dp)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Grand Total:", color = DarkText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${String.format(Locale.US, "%.2f", grandTotal)}", color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (customerName.trim().isEmpty() || customerPhone.trim().isEmpty()) {
                            Toast.makeText(viewModel.getApplication(), "Name and phone are required.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cart.isEmpty()) return@Button

                        viewModel.checkoutOrder(
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            deliveryType = deliveryType,
                            onComplete = { order ->
                                onOrderPlaced(order)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("checkout_confirm_button")
                ) {
                    Icon(Icons.Filled.ShoppingBag, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Place Order & Direct to WhatsApp", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}


// --- Circular Receipt Modal Dialog fallback ---
@Composable
fun ReceiptPopup(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onAction()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, "WhatsApp link")
                Spacer(modifier = Modifier.width(6.dp))
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Dismiss", color = MutedSlate)
            }
        },
        title = {
            Text(
                title,
                fontWeight = FontWeight.ExtraBold,
                color = SuccessGreen,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                message,
                color = DarkText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

// --- Dynamic Preferences, Themes & Options Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: RestaurantViewModel,
    onDismiss: () -> Unit
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentTextSize by viewModel.currentTextSize.collectAsState()
    val soundFXEnabled by viewModel.soundFXEnabled.collectAsState()
    val defaultPartySize by viewModel.defaultPartySize.collectAsState()

    var showStaffAuthDialog by remember { mutableStateOf(false) }
    var showStaffConsoleDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Preferences Hub ⚙️",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = DarkText
                        )
                        Text(
                            text = "Customize Ichiraku Client Experience",
                            fontSize = 11.sp,
                            color = MutedSlate,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            playAndroidChime(viewModel, false)
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedSlate)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 1: Color Themes Selection
                Text(
                    text = "SELECT APP THEME",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MutedSlate,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themesList = listOf(
                        "Orange" to Color(0xFFE65100),
                        "Blue" to Color(0xFF3F51B5),
                        "Green" to Color(0xFF2E7D32),
                        "Gold" to Color(0xFFFF8F00)
                    )

                    themesList.forEach { (name, color) ->
                        val isSelected = currentTheme == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF9F9F9))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else Color(0xFFE5E5E5),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateTheme(name)
                                    playAndroidChime(viewModel, false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color, CircleShape)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                                Text(
                                    text = name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Text Readability Font Size Scaler
                Text(
                    text = "ADJUST TEXT SIZE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MutedSlate,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizeOptions = listOf("Small", "Medium", "Large")
                    sizeOptions.forEach { opt ->
                        val isSelected = currentTextSize == opt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color(0xFFF9F9F9))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GoldPrimary else Color(0xFFE5E5E5),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateTextSize(opt)
                                    playAndroidChime(viewModel, false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) GoldPrimary else DarkText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Sound alerts toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.0f).padding(end = 8.dp)) {
                        Text(
                            text = "SOUND FEEDBACK CHIMES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MutedSlate
                        )
                        Text(
                            text = "Synthesize audio waves on reservation triggers",
                            fontSize = 9.sp,
                            color = MutedSlate.copy(alpha = 0.8f),
                            lineHeight = 12.sp
                        )
                    }
                    Switch(
                        checked = soundFXEnabled,
                        onCheckedChange = {
                            viewModel.updateSoundFXEnabled(it)
                            if (it) playAndroidChime(viewModel, true)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GoldPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Preset Default Party Guests Selector
                Text(
                    text = "DEFAULT PARTY GUESTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MutedSlate,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val guestsList = listOf(1, 2, 4, 6)
                    guestsList.forEach { count ->
                        val isSelected = defaultPartySize == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color(0xFFF9F9F9))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GoldPrimary else Color(0xFFE5E5E5),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateDefaultPartySize(count)
                                    playAndroidChime(viewModel, false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (count == 6) "6 Squad" else "$count Guest",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) GoldPrimary else DarkText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            playAndroidChime(viewModel, false)
                            showStaffAuthDialog = true
                        }
                        .testTag("staff_admin_console_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔓 Access Staff Console",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action confirmation save button
                Button(
                    onClick = {
                        playAndroidChime(viewModel, true)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Apply & Return",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showStaffAuthDialog) {
        StaffAuthDialog(
            onDismissRequest = { showStaffAuthDialog = false },
            onAuthSuccess = {
                showStaffAuthDialog = false
                showStaffConsoleDialog = true
            },
            viewModel = viewModel
        )
    }

    if (showStaffConsoleDialog) {
        StaffConsoleDialog(
            viewModel = viewModel,
            onDismissRequest = { showStaffConsoleDialog = false }
        )
    }
}

// Native audio synthesizer for client chimes
fun playAndroidChime(viewModel: RestaurantViewModel, isSuccess: Boolean) {
    if (viewModel.soundFXEnabled.value) {
        try {
            val toneG = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 65)
            if (isSuccess) {
                toneG.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 120)
            } else {
                toneG.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 80)
            }
        } catch (e: Exception) {
            // safe catch
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffAuthDialog(
    onDismissRequest: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: RestaurantViewModel
) {
    var passwordInput by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Staff Gateway 🔐",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = DarkText
                )
                Text(
                    text = "Enter Hokage authority password",
                    fontSize = 11.sp,
                    color = MutedSlate,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        hasError = false
                    },
                    modifier = Modifier.fillMaxWidth().testTag("staff_password_field"),
                    label = { Text("Password", fontSize = 12.sp) },
                    placeholder = { Text("Secret Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFFE5E5E5),
                        focusedLabelColor = GoldPrimary
                    ),
                    isError = hasError
                )

                if (hasError) {
                    Text(
                        text = "⚠️ Access Denied: Invalid Jutsu!",
                        color = Color.Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedSlate),
                        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (passwordInput == "NarutoHokage") {
                                playAndroidChime(viewModel, true)
                                onAuthSuccess()
                            } else {
                                hasError = true
                                playAndroidChime(viewModel, false)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("staff_password_submit_button")
                    ) {
                        Text("Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffConsoleDialog(
    viewModel: RestaurantViewModel,
    onDismissRequest: () -> Unit
) {
    val liveOrders by viewModel.orders.collectAsState()
    val liveBookings by viewModel.bookings.collectAsState()
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Live Orders, 1 = Reservations

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Staff Console 🍥",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = DarkText
                        )
                        Text(
                            text = "Real-time fulfillment operations",
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedSlate)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Subtab controller
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Orders SubTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedSubTab == 0) GoldPrimary else Color.Transparent)
                            .clickable { selectedSubTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Live Orders",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == 0) Color.White else MutedSlate
                        )
                    }

                    // Bookings SubTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedSubTab == 1) GoldPrimary else Color.Transparent)
                            .clickable { selectedSubTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Table Bookings",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == 1) Color.White else MutedSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic display list
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedSubTab == 0) {
                        // Live Orders Screen Content
                        if (liveOrders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No live customer orders found yet!", fontSize = 11.sp, color = MutedSlate)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(liveOrders) { order ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Order #${order.id}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = DarkText
                                                )
                                                
                                                val statusColor = when (order.status) {
                                                    "Placed" -> Color(0xFFE53935)
                                                    "Preparing" -> Color(0xFFFB8C00)
                                                    "Ready" -> Color(0xFF43A047)
                                                    else -> MutedSlate
                                                }
                                                Surface(
                                                    color = statusColor.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.padding(start = 6.dp)
                                                ) {
                                                    Text(
                                                        text = order.status,
                                                        color = statusColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Branch: ${order.branchName}", fontSize = 11.sp, color = DarkText)
                                            Text("Client: ${order.customerName} (${order.customerPhone})", fontSize = 11.sp, color = MutedSlate)
                                            Text("Items: ${order.orderItemsText}", fontSize = 11.sp, color = MutedSlate, fontStyle = FontStyle.Italic)
                                            Text("Total Paid: ₹${String.format(Locale.US, "%.2f", order.totalAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)

                                            if (order.status != "Served") {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        viewModel.advanceOrderStatus(order)
                                                        playAndroidChime(viewModel, true)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    val btnLabel = when (order.status) {
                                                        "Placed" -> "Accept & Start Cook 🍜"
                                                        "Preparing" -> "Mark Bowl Ready 🍲"
                                                        "Ready" -> "Complete & Serve 🍥"
                                                        else -> "Complete"
                                                    }
                                                    Text(btnLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Live Bookings reservations screen
                        if (liveBookings.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No reservations listed yet!", fontSize = 11.sp, color = MutedSlate)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(liveBookings) { booking ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Booking #${booking.id}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = DarkText
                                                )
                                                Text(
                                                    text = "${booking.guestsCount} Guests",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldPrimary
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Branch: ${booking.branchName}", fontSize = 11.sp, color = DarkText)
                                            Text("Guest: ${booking.customerName} (${booking.customerPhone})", fontSize = 11.sp, color = MutedSlate)
                                            Text("Schedule: ${booking.date} @ ${booking.time}", fontSize = 11.sp, color = MutedSlate)
                                            if (booking.specialRequests.trim().isNotEmpty()) {
                                                Text("Note: ${booking.specialRequests}", fontSize = 10.sp, color = Color(0xFFD84315), fontStyle = FontStyle.Italic)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
