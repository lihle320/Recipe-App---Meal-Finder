package vcmsa.projects.recipeapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recipesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        recipesContainer = findViewById(R.id.recipesContainer)
        progressBar = findViewById(R.id.progressBar)

        // Set up search button click listener
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchRecipes(query)
            } else {
                Toast.makeText(this, "Please enter a recipe name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchRecipes(query: String) {
        progressBar.visibility = View.VISIBLE
        recipesContainer.removeAllViews()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(RecipeApiService::class.java)

        apiService.searchRecipes(query).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val recipes = response.body()?.meals
                    if (recipes != null && recipes.isNotEmpty()) {
                        displayRecipes(recipes)
                    } else {
                        showNoResults()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch recipes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayRecipes(recipes: List<Recipe>) {
        recipesContainer.removeAllViews()

        recipes.forEach { recipe ->
            val recipeItem = layoutInflater.inflate(R.layout.recipe_item, null)

            val imageView = recipeItem.findViewById<ImageView>(R.id.recipeImage)
            val titleTextView = recipeItem.findViewById<TextView>(R.id.recipeTitle)
            val categoryTextView = recipeItem.findViewById<TextView>(R.id.recipeCategory)
            val areaTextView = recipeItem.findViewById<TextView>(R.id.recipeArea)
            val instructionsTextView = recipeItem.findViewById<TextView>(R.id.recipeInstructions)
            val ingredientsLayout = recipeItem.findViewById<LinearLayout>(R.id.ingredientsLayout)

            // Load image using Picasso
            Picasso.get()
                .load(recipe.strMealThumb)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(imageView)

            titleTextView.text = recipe.strMeal
            categoryTextView.text = "Category: ${recipe.strCategory}"
            areaTextView.text = "Cuisine: ${recipe.strArea}"

            // Limit instructions length
            val instructions = if (recipe.strInstructions.length > 200) {
                recipe.strInstructions.substring(0, 200) + "..."
            } else {
                recipe.strInstructions
            }
            instructionsTextView.text = instructions

            // Add ingredients
            addIngredientsToLayout(recipe, ingredientsLayout)

            // Set click listener to show details
            recipeItem.setOnClickListener {
                showRecipeDetails(recipe)
            }

            recipesContainer.addView(recipeItem)
        }
    }

    private fun addIngredientsToLayout(recipe: Recipe, layout: LinearLayout) {
        layout.removeAllViews()

        // Get all ingredients and measures
        val ingredients = listOf(
            recipe.strIngredient1 to recipe.strMeasure1,
            recipe.strIngredient2 to recipe.strMeasure2,
            recipe.strIngredient3 to recipe.strMeasure3,
            recipe.strIngredient4 to recipe.strMeasure4,
            recipe.strIngredient5 to recipe.strMeasure5,
            recipe.strIngredient6 to recipe.strMeasure6,
            recipe.strIngredient7 to recipe.strMeasure7,
            recipe.strIngredient8 to recipe.strMeasure8,
            recipe.strIngredient9 to recipe.strMeasure9,
            recipe.strIngredient10 to recipe.strMeasure10,
            recipe.strIngredient11 to recipe.strMeasure11,
            recipe.strIngredient12 to recipe.strMeasure12,
            recipe.strIngredient13 to recipe.strMeasure13,
            recipe.strIngredient14 to recipe.strMeasure14,
            recipe.strIngredient15 to recipe.strMeasure15,
            recipe.strIngredient16 to recipe.strMeasure16,
            recipe.strIngredient17 to recipe.strMeasure17,
            recipe.strIngredient18 to recipe.strMeasure18,
            recipe.strIngredient19 to recipe.strMeasure19,
            recipe.strIngredient20 to recipe.strMeasure20
        )

        // Filter out empty ingredients
        val validIngredients = ingredients.filter { (ingredient, measure) ->
            !ingredient.isNullOrEmpty() && !measure.isNullOrEmpty()
        }

        // Add ingredients to layout
        validIngredients.forEach { (ingredient, measure) ->
            val ingredientView = TextView(this).apply {
                text = "• $measure $ingredient"
                setTextAppearance(android.R.style.TextAppearance_Small)
                setPadding(0, 4, 0, 4)
            }
            layout.addView(ingredientView)
        }
    }

    private fun showNoResults() {
        val noResultsView = TextView(this).apply {
            text = "No recipes found. Try a different search term."
            gravity = android.view.Gravity.CENTER
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(0, 32, 0, 32)
        }
        recipesContainer.addView(noResultsView)
    }

    private fun showRecipeDetails(recipe: Recipe) {
        // Create a dialog to show full recipe details
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(recipe.strMeal)
            .setMessage("Category: ${recipe.strCategory}\n\nArea: ${recipe.strArea}\n\nInstructions:\n${recipe.strInstructions}")
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }
}

// API Service Interface
interface RecipeApiService {
    @GET("search.php")
    fun searchRecipes(@Query("s") query: String): Call<RecipeResponse>
}

// Data Classes
data class RecipeResponse(val meals: List<Recipe>?)

data class Recipe(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String,
    val strMealThumb: String,
    val strYoutube: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?,
    val strMeasure16: String?,
    val strMeasure17: String?,
    val strMeasure18: String?,
    val strMeasure19: String?,
    val strMeasure20: String?
)