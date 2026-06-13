package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.repository.RestaurantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RestaurantRepository
    val uiStateReady = MutableStateFlow(false)

    // Exposed Flows
    val branches: StateFlow<List<Branch>>
    val menuItems: StateFlow<List<MenuItem>>
    val bookings: StateFlow<List<Booking>>
    val orders: StateFlow<List<Order>>
    val reviews: StateFlow<List<Review>>

    // Cart Management
    private val _cart = MutableStateFlow<Map<MenuItem, Int>>(emptyMap())
    val cart: StateFlow<Map<MenuItem, Int>> = _cart.asStateFlow()

    // Navigation/Filter states
    private val _selectedBranchId = MutableStateFlow<Int?>(null)
    val selectedBranchId: StateFlow<Int?> = _selectedBranchId.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RestaurantRepository(database.restaurantDao())

        // Start initialization and seed data in background
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            uiStateReady.value = true
        }

        branches = repository.allBranches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        menuItems = repository.allMenuItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bookings = repository.allBookings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        orders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        reviews = repository.allReviews.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // --- Action Handlers ---

    fun selectBranch(id: Int?) {
        _selectedBranchId.value = id
    }

    fun addToCart(item: MenuItem) {
        val currentMap = _cart.value.toMutableMap()
        currentMap[item] = currentMap.getOrDefault(item, 0) + 1
        _cart.value = currentMap
    }

    fun removeFromCart(item: MenuItem) {
        val currentMap = _cart.value.toMutableMap()
        val count = currentMap.getOrDefault(item, 0)
        if (count <= 1) {
            currentMap.remove(item)
        } else {
            currentMap[item] = count - 1
        }
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // Booking Creation
    fun makeBooking(
        branchId: Int,
        branchName: String,
        name: String,
        phone: String,
        date: String,
        time: String,
        guests: Int,
        specialRequests: String,
        onSuccess: (Booking) -> Unit
    ) {
        viewModelScope.launch {
            val booking = Booking(
                branchId = branchId,
                branchName = branchName,
                customerName = name,
                customerPhone = phone,
                date = date,
                time = time,
                guestsCount = guests,
                specialRequests = specialRequests
            )
            val generatedId = repository.insertBooking(booking)
            val savedBooking = booking.copy(id = generatedId.toInt())
            onSuccess(savedBooking)
        }
    }

    // Order Checkout
    fun checkoutOrder(
        customerName: String,
        customerPhone: String,
        deliveryType: String,
        onComplete: (Order) -> Unit
    ) {
        val selectedId = _selectedBranchId.value ?: 1
        val selectedBranchName = branches.value.find { it.id == selectedId }?.name ?: "Leaf Village Branch"

        val cartState = _cart.value
        if (cartState.isEmpty()) return

        val total = cartState.entries.sumOf { it.key.price * it.value }
        val itemsText = cartState.entries.joinToString(", ") { "${it.key.name} x${it.value}" }

        viewModelScope.launch {
            val order = Order(
                branchId = selectedId,
                branchName = selectedBranchName,
                customerName = customerName,
                customerPhone = customerPhone,
                orderItemsText = itemsText,
                totalAmount = total,
                deliveryType = deliveryType,
                status = "Placed"
            )
            val generatedId = repository.insertOrder(order)
            val savedOrder = order.copy(id = generatedId.toInt())
            clearCart()
            onComplete(savedOrder)
        }
    }

    // Add Review
    fun addReview(
        branchId: Int,
        customerName: String,
        rating: Int,
        comment: String
    ) {
        viewModelScope.launch {
            val review = Review(
                branchId = branchId,
                customerName = customerName,
                rating = rating,
                comment = comment
            )
            repository.insertReview(review)
        }
    }

    // Generating formatted WhatsApp texts
    fun getWhatsAppTextForOrder(order: Order, phoneToContact: String): String {
        return "Konnichiwa Ichiraku Ramen! 🍜\n" +
                "I would like to place an order at *${order.branchName}*:\n" +
                "--------------------------------------\n" +
                "👤 *Name*: ${order.customerName}\n" +
                "📞 *Contact*: ${order.customerPhone}\n" +
                "🚚 *Service Type*: ${order.deliveryType}\n" +
                "--------------------------------------\n" +
                "📝 *Items Ordered*:\n" +
                order.orderItemsText.split(", ").joinToString("\n") { "• $it" } + "\n" +
                "--------------------------------------\n" +
                "💰 *Grand Total*: ₹${String.format(Locale.US, "%.2f", order.totalAmount)}\n\n" +
                "Please verify my order details. Thank you!"
    }

    fun getWhatsAppTextForBooking(booking: Booking, phoneToContact: String): String {
        val requestsText = if (booking.specialRequests.trim().isEmpty()) "None" else booking.specialRequests
        return "Konnichiwa Ichiraku Ramen! 📅\n" +
                "I would like to reserve a table at *${booking.branchName}*:\n" +
                "--------------------------------------\n" +
                "👤 *Name*: ${booking.customerName}\n" +
                "📞 *Contact*: ${booking.customerPhone}\n" +
                "👥 *Guests Count*: ${booking.guestsCount} Persons\n" +
                "📅 *Reservation Date*: ${booking.date}\n" +
                "🕒 *Time Slot*: ${booking.time}\n" +
                "💬 *Special Request*: $requestsText\n" +
                "--------------------------------------\n\n" +
                "Please confirm this table booking at your earliest. Thank you!"
    }
}
