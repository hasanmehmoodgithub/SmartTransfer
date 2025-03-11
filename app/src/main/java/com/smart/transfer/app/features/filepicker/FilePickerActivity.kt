package com.smart.transfer.app.features.filepicker
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter.FilePickerCategoryAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter.FilePickerViewPagerAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.FilePickerCategory


class FilePickerActivity : AppCompatActivity() {
    private lateinit var categoryAdapter: FilePickerCategoryAdapter
    private lateinit var viewPager: ViewPager2

    private val categories = listOf(
        FilePickerCategory("Images", R.drawable.ic_folder),
        FilePickerCategory("Videos", R.drawable.ic_folder),
        FilePickerCategory("Audio", R.drawable.ic_folder),
        FilePickerCategory("Documents", R.drawable.ic_folder)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)


        val recyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
        viewPager = findViewById(R.id.viewPager)

        categoryAdapter = FilePickerCategoryAdapter(categories) { index ->
            viewPager.currentItem = index
            categoryAdapter.updateSelection(index)
        }

        recyclerView.adapter = categoryAdapter

        viewPager.adapter = FilePickerViewPagerAdapter(this)
        viewPager.isUserInputEnabled = false
    }
}

class ImagesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_test, container, false)
    }
}

