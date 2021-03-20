package jp.techacademy.masahito.chikami.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    var flag = true

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {  //snapshotはその時点の状態をそのまま保存したもの
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

    private val mFavoriteListener = object : ChildEventListener {

        //ChildAddedはすでにデータがある場合にしか呼ばれない
        //からこのメソッドが呼ばれたときは登録済み
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            favoritebutton.setImageResource(R.drawable.ic_star)
            flag = false
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

        val user = FirebaseAuth.getInstance().currentUser

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
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

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
            .child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }

        override fun onResume() {
            super.onResume()

            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {     //ログインしていない場合
                favoritebutton.visibility = View.GONE
            } else {               //ログインしている場合
                favoritebutton.visibility = View.VISIBLE

                val dataBaseReference = FirebaseDatabase.getInstance().reference
                mFavoriteRef = dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
                val data = HashMap<String, String>()

                mFavoriteRef.addChildEventListener(mFavoriteListener)

                //お気に入りボタン押した時
                favoritebutton.setOnClickListener {
                    if (flag) {
                        favoritebutton.setImageResource(R.drawable.ic_star)
                        Log.d("test","お気に入りに追加")
                        //Firebaseに登録
                        data["genre"] = mQuestion.genre.toString()
                        mFavoriteRef.setValue(data)
                        flag = false
                    } else {
                        favoritebutton.setImageResource(R.drawable.ic_star_border)
                        Log.d("test","お気に入り削除")
                        //登録削除
                        mFavoriteRef.removeValue()
                        flag = true
                    }
                }
            }
        }
}