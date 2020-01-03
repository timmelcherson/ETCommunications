package com.uu1te721.etcommunications.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uu1te721.etcommunications.R;
import com.uu1te721.etcommunications.activities.ImageDialog;
import com.uu1te721.etcommunications.uicomponents.MessageCard;

import java.util.List;

import static com.uu1te721.etcommunications.utils.Constants.TAG;


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

        ViewHolder(@NonNull View view) {
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
        } else { // Sent
            holder.mMessageTV.setBackground(context.getDrawable(R.drawable.sent_message_card_background));
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        holder.mMessageTV.setLayoutParams(params);


        // Putting text or multimedia object to bubbles.
        SpannableStringBuilder ssb = new SpannableStringBuilder();
//        Log.d(TAG, "bitmap of item at position " + position + " null in recycler?: " + item.hasPicture(position));
        if (item.hasPicture(position)) {
            Log.d(TAG, "Item has picture at position: " + position);
            ssb.append(" ");
            Bitmap pic = item.getPicture();
            ssb.setSpan(new ImageSpan(context, pic), ssb.length() - 1, ssb.length(), 0);

            // Make figure clickable for preview.
            holder.mMessageTV.setOnClickListener(view -> {
                Intent intent = new Intent(context, ImageDialog.class);
                intent.putExtra("imagePath", item.getPhotoPath());
                Log.d(TAG, "Item get photo path: " + item.getPhotoPath());
                context.startActivity(intent);
            });

        } else {// If only has text
            ssb.append(item.getText());
        }
        holder.mMessageTV.setText(ssb);

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public interface OnMessageClickListener {
        void onItemClick(int position);
    }
}
