package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // --- Branch Queries ---
    @Query("SELECT * FROM branches ORDER BY id ASC")
    fun getAllBranches(): Flow<List<Branch>>

    @Query("SELECT * FROM branches WHERE id = :id")
    fun getBranchById(id: Int): Flow<Branch?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<Branch>)


    // --- Menu Queries ---
    @Query("SELECT * FROM menu_items ORDER BY category, name ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(menuItems: List<MenuItem>)


    // --- Booking Queries ---
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE branchId = :branchId ORDER BY timestamp DESC")
    fun getBookingsForBranch(branchId: Int): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)


    // --- Order Queries ---
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE branchId = :branchId ORDER BY timestamp DESC")
    fun getOrdersForBranch(branchId: Int): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long


    // --- Review Queries ---
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE branchId = :branchId ORDER BY timestamp DESC")
    fun getReviewsForBranch(branchId: Int): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)
}
