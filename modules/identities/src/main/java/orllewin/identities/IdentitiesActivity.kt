package orllewin.identities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import orllewin.identities.db.Identities
import orllewin.extensions.hide
import orllewin.extensions.mainThread
import orllewin.extensions.show
import orllewin.gemini.Gemini
import orllewin.gemini.identity.Identity
import orllewin.identities.databinding.ActivityCertificatesBinding
import orllewin.lib.certifikat.CertGen
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import kotlin.concurrent.thread


class IdentitiesActivity: AppCompatActivity() {

    lateinit var identities: Identities
    private lateinit var binding: ActivityCertificatesBinding
    private lateinit var adapter: IdentityAdapter
    private val aliases = mutableListOf<String>()

    private var suppliedHost: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCertificatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        suppliedHost = intent.getStringExtra("host")

        adapter = IdentityAdapter(
            onIdentity = { identity ->
                edit(identity)
            },
            onOverflow = {view, identity ->
                showIdentityOverflow(view, identity)
            })

        binding.identitiesRecycler.layoutManager = LinearLayoutManager(this)
        binding.identitiesRecycler.adapter = adapter

        identities = Identities(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)

        binding.empty.show()

        binding.createNewButton.setOnClickListener {
            IdentityDialog(this, suppliedHost, null, aliases){ _, _ ->
                refresh()
            }.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh(){
        aliases.clear()
        CertGen.logKeystore()
        identities.get { identities ->
            Gemini.updateIdentities(identities)
            identities.forEach { identity ->
                aliases.add(identity.alias)
            }
            mainThread {
                when {
                    identities.isEmpty() -> {
                        binding.identitiesRecycler.hide()
                        binding.empty.show()
                    }
                    else -> {
                        adapter.update(identities)
                        binding.identitiesRecycler.show()
                        binding.empty.hide()
                    }
                }
            }
        }
    }

    private fun showIdentityOverflow(view: View, identity: Identity){
        //onOverflow
        val identityOverflow = PopupMenu(this, view)

        identityOverflow.inflate(R.menu.menu_identity)

        identityOverflow.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_identity_export -> export(identity)
                R.id.menu_identity_delete -> delete(identity)
            }
            true
        }

        identityOverflow.show()
    }

    @Suppress("unused")
    private fun export(identity: Identity) {

        thread{
            val cert = CertGen.get(identity.alias)
            val certPEM = CertGen.certToPem(cert)

            val keyFactory = KeyFactory.getInstance("RSA")
            val key = keyFactory.generatePrivate(PKCS8EncodedKeySpec(identity.privateKey))
            val privateKeyPem = CertGen.privateKeyToPem(key)

            val exportCert = "$certPEM\n$privateKeyPem"

            mainThread {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, exportCert)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }
    }

    private fun edit(identity: Identity){
        IdentityDialog(this, null, identity, aliases){ _, _ ->
            refresh()
        }.show()
    }

    /**
     *
     * Bookmark isn't actually deleted from the DB until the SnackBar disappears. Which is nice.
     *
     */
    @SuppressLint("ShowToast")
    private fun delete(identity: Identity){
        //OnDelete
        adapter.hide(identity)
        Snackbar.make(binding.root, "Deleted ${identity.name}", Snackbar.LENGTH_SHORT).addCallback(
            object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) = when (event) {
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION -> adapter.show(identity)
                    else -> identities.delete(identity) {
                        mainThread {
                            adapter.remove(identity)
                            refresh()
                        }
                    }
                }
            }).setAction("Undo"){
            //Action listener unused
        }.show()
    }
}