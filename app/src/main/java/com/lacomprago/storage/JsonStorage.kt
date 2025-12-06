package com.lacomprago.storage

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lacomprago.model.CachedOrderList
import com.lacomprago.model.DownloadedOrders
import com.lacomprago.model.ProcessedOrders
import com.lacomprago.model.ProductList
import java.io.FileNotFoundException
import java.io.IOException

/**
 * JSON-based storage for products and processed orders.
 * Files are stored in app-private storage and automatically removed on app uninstall.
 *
 * @property context Application context for file operations
 */
class JsonStorage(private val context: Context) {
    
    private val gson = Gson()
    
    /**
     * Save the product list to products.json.
     *
     * @param productList The product list to save
     * @throws JsonStorageException if saving fails
     */
    fun saveProductList(productList: ProductList) {
        try {
            val json = gson.toJson(productList)
            context.openFileOutput(PRODUCTS_FILE, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving product list", e)
            throw JsonStorageException("Failed to save products: ${e.message}", e)
        }
    }
    
    /**
     * Load the product list from products.json.
     *
     * @return The loaded product list, or null if the file doesn't exist
     * @throws JsonStorageException if loading fails due to parsing or I/O errors
     */
    fun loadProductList(): ProductList? {
        return try {
            context.openFileInput(PRODUCTS_FILE).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                gson.fromJson(json, ProductList::class.java)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Products file not found, returning null")
            null
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing products JSON", e)
            throw JsonStorageException("Failed to parse products file: ${e.message}", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading products file", e)
            throw JsonStorageException("Failed to read products file: ${e.message}", e)
        }
    }
    
    /**
     * Save processed orders to processed_orders.json.
     *
     * @param processedOrders The processed orders to save
     * @throws JsonStorageException if saving fails
     */
    fun saveProcessedOrders(processedOrders: ProcessedOrders) {
        try {
            val json = gson.toJson(processedOrders)
            context.openFileOutput(PROCESSED_ORDERS_FILE, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving processed orders", e)
            throw JsonStorageException("Failed to save processed orders: ${e.message}", e)
        }
    }
    
    /**
     * Load processed orders from processed_orders.json.
     *
     * @return The loaded processed orders, or an empty ProcessedOrders if the file doesn't exist
     * @throws JsonStorageException if loading fails due to parsing or I/O errors
     */
    fun loadProcessedOrders(): ProcessedOrders {
        return try {
            context.openFileInput(PROCESSED_ORDERS_FILE).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                gson.fromJson(json, ProcessedOrders::class.java)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Processed orders file not found, returning empty")
            ProcessedOrders()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing processed orders JSON", e)
            throw JsonStorageException("Failed to parse processed orders file: ${e.message}", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading processed orders file", e)
            throw JsonStorageException("Failed to read processed orders file: ${e.message}", e)
        }
    }
    
    /**
     * Delete the products file.
     *
     * @return true if the file was deleted, false otherwise
     */
    fun deleteProductList(): Boolean {
        return context.deleteFile(PRODUCTS_FILE)
    }
    
    /**
     * Delete the processed orders file.
     *
     * @return true if the file was deleted, false otherwise
     */
    fun deleteProcessedOrders(): Boolean {
        return context.deleteFile(PROCESSED_ORDERS_FILE)
    }
    
    /**
     * Check if the products file exists.
     *
     * @return true if the file exists, false otherwise
     */
    fun hasProductList(): Boolean {
        return context.getFileStreamPath(PRODUCTS_FILE).exists()
    }
    
    /**
     * Check if the processed orders file exists.
     *
     * @return true if the file exists, false otherwise
     */
    fun hasProcessedOrders(): Boolean {
        return context.getFileStreamPath(PROCESSED_ORDERS_FILE).exists()
    }
    
    /**
     * Save the cached order list to cached_orders.json.
     *
     * @param cachedOrderList The cached order list to save
     * @throws JsonStorageException if saving fails
     */
    fun saveCachedOrderList(cachedOrderList: CachedOrderList) {
        try {
            val json = gson.toJson(cachedOrderList)
            context.openFileOutput(CACHED_ORDERS_FILE, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving cached order list", e)
            throw JsonStorageException("Failed to save cached order list: ${e.message}", e)
        }
    }
    
    /**
     * Load cached order list from cached_orders.json.
     *
     * @return The loaded cached order list, or null if the file doesn't exist
     * @throws JsonStorageException if loading fails due to parsing or I/O errors
     */
    fun loadCachedOrderList(): CachedOrderList? {
        return try {
            context.openFileInput(CACHED_ORDERS_FILE).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                gson.fromJson(json, CachedOrderList::class.java)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Cached order list file not found")
            null
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing cached order list JSON", e)
            throw JsonStorageException("Failed to parse cached order list file: ${e.message}", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading cached order list file", e)
            throw JsonStorageException("Failed to read cached order list file: ${e.message}", e)
        }
    }
    
    /**
     * Check if the cached order list file exists.
     *
     * @return true if the file exists, false otherwise
     */
    fun hasCachedOrderList(): Boolean {
        return context.getFileStreamPath(CACHED_ORDERS_FILE).exists()
    }
    
    /**
     * Delete the cached order list file.
     *
     * @return true if the file was deleted, false otherwise
     */
    fun deleteCachedOrderList(): Boolean {
        return context.deleteFile(CACHED_ORDERS_FILE)
    }
    
    /**
     * Save downloaded orders to downloaded_orders.json.
     *
     * @param downloadedOrders The downloaded orders to save
     * @throws JsonStorageException if saving fails
     */
    fun saveDownloadedOrders(downloadedOrders: DownloadedOrders) {
        try {
            val json = gson.toJson(downloadedOrders)
            context.openFileOutput(DOWNLOADED_ORDERS_FILE, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving downloaded orders", e)
            throw JsonStorageException("Failed to save downloaded orders: ${e.message}", e)
        }
    }
    
    /**
     * Load downloaded orders from downloaded_orders.json.
     *
     * @return The loaded downloaded orders, or an empty DownloadedOrders if the file doesn't exist
     * @throws JsonStorageException if loading fails due to parsing or I/O errors
     */
    fun loadDownloadedOrders(): DownloadedOrders {
        return try {
            context.openFileInput(DOWNLOADED_ORDERS_FILE).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                gson.fromJson(json, DownloadedOrders::class.java)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Downloaded orders file not found, returning empty")
            DownloadedOrders()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing downloaded orders JSON", e)
            throw JsonStorageException("Failed to parse downloaded orders file: ${e.message}", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading downloaded orders file", e)
            throw JsonStorageException("Failed to read downloaded orders file: ${e.message}", e)
        }
    }
    
    /**
     * Check if the downloaded orders file exists.
     *
     * @return true if the file exists, false otherwise
     */
    fun hasDownloadedOrders(): Boolean {
        return context.getFileStreamPath(DOWNLOADED_ORDERS_FILE).exists()
    }
    
    /**
     * Delete the downloaded orders file.
     *
     * @return true if the file was deleted, false otherwise
     */
    fun deleteDownloadedOrders(): Boolean {
        return context.deleteFile(DOWNLOADED_ORDERS_FILE)
    }
    
    companion object {
        private const val TAG = "JsonStorage"
        const val PRODUCTS_FILE = "products.json"
        const val PROCESSED_ORDERS_FILE = "processed_orders.json"
        const val CACHED_ORDERS_FILE = "cached_orders.json"
        const val DOWNLOADED_ORDERS_FILE = "downloaded_orders.json"
    }
}

/**
 * Exception thrown when JSON storage operations fail.
 *
 * @property message Description of the error
 * @property cause The underlying exception
 */
class JsonStorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
