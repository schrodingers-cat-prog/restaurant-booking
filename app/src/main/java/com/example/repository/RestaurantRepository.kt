package com.example.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RestaurantRepository(private val dao: RestaurantDao) {

    val allBranches: Flow<List<Branch>> = dao.getAllBranches()
    val allMenuItems: Flow<List<MenuItem>> = dao.getAllMenuItems()
    val allBookings: Flow<List<Booking>> = dao.getAllBookings()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    val allReviews: Flow<List<Review>> = dao.getAllReviews()

    fun getBranchById(id: Int): Flow<Branch?> = dao.getBranchById(id)
    fun getBookingsForBranch(branchId: Int): Flow<List<Booking>> = dao.getBookingsForBranch(branchId)
    fun getReviewsForBranch(branchId: Int): Flow<List<Review>> = dao.getReviewsForBranch(branchId)
    fun getOrdersForBranch(branchId: Int): Flow<List<Order>> = dao.getOrdersForBranch(branchId)

    suspend fun insertBooking(booking: Booking): Long = dao.insertBooking(booking)
    suspend fun updateBooking(booking: Booking) = dao.updateBooking(booking)
    suspend fun deleteBooking(booking: Booking) = dao.deleteBooking(booking)

    suspend fun insertOrder(order: Order): Long = dao.insertOrder(order)
    suspend fun updateOrder(order: Order) = dao.updateOrder(order)
    suspend fun insertReview(review: Review) = dao.insertReview(review)

    // Preseed static data if database is empty
    suspend fun checkAndSeedDatabase() {
        val branches = dao.getAllBranches().first()
        if (branches.isEmpty()) {
            val initialBranches = listOf(
                Branch(
                    id = 1,
                    name = "Leaf Village Branch",
                    address = "Street 1, Near Hokage Monument, Hidden Leaf Village, 10001",
                    phone = "+91 98765 43210",
                    description = "The original Ichiraku Ramen branch. Sitting right at the heart of Konohagakure, where legendary ninjas like Naruto Uzumaki came to fuel their wills of fire over hot ramen.",
                    imageLabel = "heritage",
                    openingHours = "10:00 AM - 11:30 PM",
                    latitude = 35.0116,
                    longitude = 135.7681
                ),
                Branch(
                    id = 2,
                    name = "Sand Village Branch",
                    address = "Hidden Oasis Vista, Sandstorms Plaza, Sunagakure, 20002",
                    phone = "+91 98765 43211",
                    description = "Our desert outpost brings the rich warmth of specialized ramen broth to the sands. Featuring cool wind-sheltered seating, spicy dust-crafted ramen, and refreshing melon chillers.",
                    imageLabel = "bistro",
                    openingHours = "11:00 AM - 12:30 AM",
                    latitude = 34.6937,
                    longitude = 135.5023
                ),
                Branch(
                    id = 3,
                    name = "Mist Village Branch",
                    address = "Waterfront Harbor Line, Lake Kirigakure, Hidden Mist, 30003",
                    phone = "+91 98765 43212",
                    description = "A serene lakeside parlor shrouded in gentle steam. Best known for our seafood-infused miso varieties, local coastal premium sides, and tranquil candle-lit docks.",
                    imageLabel = "coastal",
                    openingHours = "11:30 AM - 11:00 PM",
                    latitude = 35.4437,
                    longitude = 139.6380
                )
            )
            dao.insertBranches(initialBranches)

            // Seed initial reviews so branches already have some visual engagement and data
            val initialReviews = listOf(
                Review(branchId = 1, customerName = "Naruto Uzumaki", rating = 5, comment = "The Miso Chashu Ramen is the best food in the entire ninja world! Dattebayo! I eat here every single day!", timestamp = System.currentTimeMillis() - 86400000),
                Review(branchId = 1, customerName = "Kakashi Hatake", rating = 5, comment = "A quiet, excellent spot to read my favorite novels while sipping rich, masterfully-brewed savory broth.", timestamp = System.currentTimeMillis() - 172800000),
                Review(branchId = 2, customerName = "Gaara of the Sand", rating = 5, comment = "The spicy chili ramen warms the soul. Highly appreciate the quiet and peaceful atmosphere here.", timestamp = System.currentTimeMillis() - 50000000),
                Review(branchId = 2, customerName = "Temari", rating = 4, comment = "Extremely flavorful. Perfect spicy kick to combat the cool desert nights.", timestamp = System.currentTimeMillis() - 120000000),
                Review(branchId = 3, customerName = "Mei Terumi", rating = 5, comment = "Fabulous seafood broth, and the steaming environment feels just like home. Absolutely outstanding hospitality.", timestamp = System.currentTimeMillis() - 250000000)
            )
            for (rev in initialReviews) {
                dao.insertReview(rev)
            }
        }

        val menuItems = dao.getAllMenuItems().first()
        if (menuItems.isEmpty()) {
            val initialMenuItems = listOf(
                MenuItem(1, "Naruto Special Miso Ramen", "Mains", 350.0, "Our flagship miso ramen with soft noodles, rich soybean broth, generous narutomaki swirls, soft boiled egg, and nori.", true, "miso_ramen"),
                MenuItem(2, "Hokage Shoyu Ramen", "Mains", 330.0, "Classic soy broth ramen cooked with organic bamboo shoots, toasted sesame oil, premium spring onions, and a perfectly-cured egg.", true, "shoyu_ramen"),
                MenuItem(3, "Volcano Spicy Tonkotsu Ramen", "Mains", 380.0, "Spicy, rich, and creamy bone-style savory broth (pork-free / vegetarian option) with chili oil paste, dry seaweed, and wood ear mushrooms.", true, "spicy_ramen"),
                MenuItem(4, "Mist Seafood Curry Ramen", "Mains", 410.0, "Rich curry-infused ramen broth loaded with plump fresh prawns, calamari rings, seaweed flakes, and soft noodles.", true, "seafood_ramen"),
                MenuItem(5, "Steamed Pork-free Gyoza", "Appetizers", 220.0, "Pan-seared Japanese dumplings with seasoned vegetable and tofu fillings, served with signature dipping vinegar.", true, "gyoza"),
                MenuItem(6, "Classic Takoyaki Balls", "Appetizers", 240.0, "Crispy fried round batter pockets with savory fillings, drizzled with takoyaki sauce, spiced mayo, and seaweed bits.", true, "takoyaki"),
                MenuItem(7, "Garlic Salted Edamame", "Appetizers", 140.0, "Steamed vibrant soy bean pods tossed in coarse sea salt and charred garlic chili oil bits.", true, "edamame"),
                MenuItem(8, "Sweet Sakura Mochi", "Desserts", 180.0, "Sweet pink glutinous rice cake with a core of sweet red-bean paste, wrapped in a pickled cherry blossom leaf.", true, "mochi"),
                MenuItem(9, "Matcha Ice Cream Bowl", "Desserts", 190.0, "Rich, bitter-sweet premium stone-ground Uji matcha green tea ice cream topped with sweet red azuki beans.", true, "matcha_ice"),
                MenuItem(10, "Bubbly Melon Soda", "Drinks", 150.0, "Iconic vibrant green melon-flavored Japanese carbonated soda, served ice-cold with a cherry garnish.", true, "melon_soda"),
                MenuItem(11, "Premium Hot Sencha", "Drinks", 110.0, "Freshly brewed high-quality Japanese loose-leaf green tea with sweet grassy notes, served in a traditional clay teapot.", true, "sencha")
            )
            dao.insertMenuItems(initialMenuItems)
        }
    }
}
