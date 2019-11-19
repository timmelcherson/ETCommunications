package com.uu1te721.etcommunications;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.uu1te721.etcommunications.MessengerActivity.TAG;

public class MessengerRecyclerViewAdapter extends RecyclerView.Adapter<MessengerRecyclerViewAdapter.ViewHolder> {


    private Context context;
    private List<MessageCard> mMessageList;
    private String msgDirection;

    // Constructor
    public MessengerRecyclerViewAdapter(Context context, List<MessageCard> messageList) {
        this.context = context;
        this.mMessageList = messageList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mMessageTV;
        private RelativeLayout mMessageLayout;
        private String mMsgDirection;

        public ViewHolder(@NonNull View view) {
            super(view);
            mMessageTV = view.findViewById(R.id.message_card_layout_text);
            mMessageLayout = view.findViewById(R.id.message_card_layout);
        }

        @Override
        public void onClick(View v) {

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        MessageCard item = mMessageList.get(position);
        msgDirection = item.getMessageDirection();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if (msgDirection.equals("received")) {
            holder.mMessageTV.setBackground(context.getDrawable(R.drawable.received_message_card_background));
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        }
        else {
            holder.mMessageTV.setBackground(context.getDrawable(R.drawable.sent_message_card_background));
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        holder.mMessageTV.setLayoutParams(params);
        holder.mMessageTV.setText(item.getText());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public interface OnMessageClickListener {
        void onItemClick(int position);
    }
}
