package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "branches")
data class Branch(
    @PrimaryKey val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val description: String,
    val imageLabel: String,
    val openingHours: String,
    val latitude: Double,
    val longitude: Double
) : Serializable

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val isVegetarian: Boolean,
    val imageResourceName: String
) : Serializable

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branchId: Int,
    val branchName: String,
    val customerName: String,
    val customerPhone: String,
    val date: String,
    val time: String,
    val guestsCount: Int,
    val specialRequests: String,
    val tableId: String? = null,
    val tableName: String? = null,
    val tableSeats: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isConfirmed: Boolean = false
) : Serializable

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branchId: Int,
    val branchName: String,
    val customerName: String,
    val customerPhone: String,
    val orderItemsText: String, // Storing serialized format e.g. "Paneer Tikka x2, Butter Naan x3"
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryType: String, // "Dine-In", "Takeaway", "Delivery"
    val status: String = "Placed"
) : Serializable

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branchId: Int,
    val customerName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
