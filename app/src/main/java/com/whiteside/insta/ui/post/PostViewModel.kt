package com.whiteside.insta.ui.post

import com.whiteside.insta.model.Post
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.whiteside.insta.R
import com.whiteside.insta.model.BlobBitmap
import com.whiteside.insta.model.Date_Time
import com.whiteside.insta.model.Profile
import com.whiteside.insta.ui.wall.WallActivity

class PostViewModel : ViewModel() {

    var post: Post? = null
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var profile = MutableLiveData<Profile?>()
    val fStore = FirebaseFirestore.getInstance()

    companion object {
        @JvmStatic
        @BindingAdapter("image")
        fun getImage(view: ImageView, blob: Blob?) {
            if (blob != null)
                view.setImageBitmap(BlobBitmap.convertBlobToBitmap(blob))
            else
                view.setImageResource(R.drawable.avatar05)
        }

        @JvmStatic
        @BindingAdapter("profile_image")
        fun loadProfilePic(view: ImageView, profile: Profile?) {
            profile?.let {
                view.setImageBitmap(BlobBitmap.convertBlobToBitmap(it.profilePic))
            }
        }

        @JvmStatic
        @BindingAdapter("love")
        fun checkLike(view: ImageView, post: Post?) {
            if (post != null) {
                if (post.likes!!.containsKey(FirebaseAuth.getInstance().uid))
                    view.setImageResource(R.drawable.loved)
                else
                    view.setImageResource(R.drawable.love)
            }
        }

        @JvmStatic
        @BindingAdapter("post_image")
        fun loadPostImage(view: ImageView, post: Post?) {
            view.setImageBitmap(BlobBitmap.convertBlobToBitmap(post?.postImage))
        }

        @JvmStatic
        @BindingAdapter("post_date")
        fun loadPostDate(view: TextView, post: Post?) {
            view.text = Date_Time.timeFromNow(post?.time)
        }

        @JvmStatic
        @BindingAdapter("post_likes")
        fun loadPostLikes(view: TextView, post: Post?) {
            if (post != null)
                view.text = post.likes!!.size.toString()
        }
    }

    fun likeClicked(view: View, post: Post?) {
        if (post != null) {

            val liked = post.likes!!.contains(auth.uid)

            if (liked) {
                removeLike(post)
                (view as ImageView).setImageResource(R.drawable.love)
            } else {
                setLike(post)
                (view as ImageView).setImageResource(R.drawable.loved)
            }
        }
    }

    private fun removeLike(post: Post) {
        post.likes!!.remove(auth.uid!!)
        updateLikes(post)
    }

    private fun setLike(post: Post) {
        post.likes!![auth.uid!!] = Timestamp.now()
        updateLikes(post)
    }

    private fun updateLikes(post: Post) {
        fStore.collection("Users")
            .document(post.UID!!)
            .collection("Posts")
            .document(post.Id!!)
            .update("likes", post.likes)
            .addOnSuccessListener {
                post.notifyChange()
            }
    }

    fun loadProfile(UID: String?) {
        fStore.collection("Users")
            .document(UID ?: "")
            .get()
            .addOnSuccessListener {
                profile.value = it.toObject(Profile::class.java)
            }
    }

    fun addClicked(view: View, post: Post) {
        if (post.postImage != null) {
            view.isEnabled = false
        } else {
            Toast.makeText(view.context, "Add an image firstly", Toast.LENGTH_LONG).show()
            return
        }

        //TODO How to show progress bar on a context
        val auth = FirebaseAuth.getInstance()

        post.time = Timestamp.now()
        post.UID = auth.uid
        post.Id = Timestamp.now().seconds.toString()

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(auth.currentUser!!.uid)
            .collection("Posts")
            .document(post.Id!!)
            .set(post)
            .addOnSuccessListener {
                Toast.makeText(view.context, "The image added Successfully", Toast.LENGTH_LONG)
                    .show()
                (view.context as Activity).startActivity(Intent(view.context as Activity, WallActivity::class.java))
                (view.context as Activity).finish()
            }
            .addOnFailureListener {
                Toast.makeText(view.context, "Failed to upload the image", Toast.LENGTH_LONG).show()
            }
    }
}