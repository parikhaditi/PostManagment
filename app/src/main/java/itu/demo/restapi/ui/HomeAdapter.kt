package itu.demo.restapi.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import itu.demo.R
import itu.demo.restapi.data.ResponseDataModel
import kotlinx.android.synthetic.main.home_rv_item_view.view.*

class HomeAdapter(var listener:HomeListener) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>(){

    private var data : ArrayList<ResponseDataModel>?=null

    interface HomeListener{
        fun onItemDeleted(postModel: ResponseDataModel, position: Int)
    }

    fun setData(list: ArrayList<ResponseDataModel>){
        data = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_rv_item_view, parent, false))
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = data?.get(position)
        holder.bindView(item)

        holder.itemView.tv_remove.setOnClickListener {
            item?.let { it1 ->
                listener.onItemDeleted(it1, position)
            }
        }

        holder.itemView.tv_like.setOnClickListener {
            data?.get(position)?.liked = !data?.get(position)?.liked!!
            if (item?.liked == true) {
                holder.itemView.tv_like.text = "UnLike"
                holder.itemView.tv_like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_filled,0,0,0)
            }
            else
            {
                holder.itemView.tv_like.text = "Like"
                holder.itemView.tv_like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_unfilled,0,0,0)
            }
        }
    }

    fun removeData(position: Int) {
        data?.removeAt(position)
        notifyDataSetChanged()
    }

    class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bindView(item: ResponseDataModel?) {

            itemView.tv_home_item_id.text = (layoutPosition+1).toString()+". "
            itemView.tv_home_item_title.text = item?.title.toString().capitalize()
            itemView.tv_home_item_body.text = item?.body.toString().capitalize()
            if (item?.liked == true) {
                itemView.tv_like.text = "UnLike"
                itemView.tv_like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_filled,0,0,0)
            }
            else
            {
                itemView.tv_like.text = "Like"
                itemView.tv_like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_unfilled,0,0,0)
            }
            if(layoutPosition>99) itemView.tv_remove.visibility = View.GONE
        }

    }

}