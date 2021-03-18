package jp.techacademy.masahito.chikami.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef :DatabaseReference
    val dataBaseReference = FirebaseDatabase.getInstance().reference


    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // TODO:
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---

            }
        }

        //課題：画面表示したとき
        //①firebase参照しお気に入りにかどうか参照しておく
        //②ログインしていないときはボタンを隠す?反応しなくする

        //ログイン済みユーザーを取得
        val user = FirebaseAuth.getInstance().currentUser
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        //ログインしてなかったらfavoritebutton隠す,してたら表示
        if (user == null){
            favoritebutton.visibility = View.GONE
        }else{
            favoritebutton.visibility = View.VISIBLE
        }

        //課題：favoritebuttonのclickリスナー
        //お気に入りをまずどこかに保持させる
        //firebaseを参照する方法考える
        //クリックしたらお気に入りに登録/解除(firebase参照)
        //お気に入り登録/解除で色変わるようにする


        favoritebutton.setOnClickListener {
            Log.d("test","favoritebutton押した")
            val mFavoriteRef = dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
            val data = HashMap<String,String>()

            if(mFavoriteRef != null){
                mFavoriteRef.child(mQuestion.questionUid).removeValue()
                Log.d("test","favoriteから削除")
                favoritebutton.setImageResource(R.drawable.ic_star_border)
            }else{
                data["genre"] = mQuestion.genre.toString()
                mFavoriteRef.setValue(data)
                Log.d("test","favoriteに登録")
                favoritebutton.setImageResource(R.drawable.ic_star)
            }
        }
    }
}