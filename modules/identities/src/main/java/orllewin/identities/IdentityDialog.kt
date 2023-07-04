package orllewin.identities

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.google.android.material.snackbar.Snackbar
import orllewin.identities.db.Identities
import orllewin.extensions.mainThread
import orllewin.gemini.identity.Identity
import orllewin.gemini.identity.IdentityRule
import orllewin.identities.databinding.DialogIdentityBinding
import orllewin.lib.certifikat.CertGen
import kotlin.concurrent.thread


class IdentityDialog(
    context: Context,
    val suppliedHost: String?,
    val identity: Identity?,
    val aliases: List<String>,
    val onDismiss: (label: String?, uri: String?) -> Unit): AppCompatDialog(context, R.style.ResourcesMainTheme) {
    var isEdit = false
    private val identities = Identities(context)
    private var binding: DialogIdentityBinding

    init {
        val view = View.inflate(context, R.layout.dialog_identity, null)
        binding = DialogIdentityBinding.bind(view)
        setContentView(binding.root)

        binding.certUnassigned.isChecked = true

        when {
            identity == null && suppliedHost != null -> {
                binding.identityUri.setText("gemini://$suppliedHost")
            }
        }

        identity?.let{
            isEdit = true
            binding.identitiesToolbar.title = context.resources.getString(R.string.edit_identity)
            binding.identityName.setText(identity.name)
            binding.identityUri.setText(identity.uri.toString())
            when(identity.rule){
                IdentityRule.Unspecified().key -> binding.certUnassigned.isChecked = true
                IdentityRule.EntireDomain().key -> binding.certUseDomain.isChecked = true
                IdentityRule.SpecificUrl().key -> binding.certUseAddress.isChecked = true
            }
            showCertInfo(identity)
        }

        binding.identitiesToolbar.setNavigationIcon(R.drawable.vector_close)
        binding.identitiesToolbar.setNavigationOnClickListener {
            onDismiss(null, null)
            dismiss()
        }

        binding.identitiesToolbar.inflateMenu(R.menu.add_identity)
        binding.identitiesToolbar.setOnMenuItemClickListener { menuItem ->
            if(menuItem.itemId == R.id.menu_action_save_identity){
               save()
            }

            true

        }
    }

    private fun showCertInfo(identity: Identity) {
        binding.certData.append("Alias: ${identity.alias}\n")
        thread{
            CertGen.get(identity.alias){ certificate ->
                mainThread {
                    val certStr = "${certificate}".lines().map { line ->
                        line.trim()
                    }.joinToString("\n")
                    binding.certData.append(certStr)
                }
            }
        }

    }

    private fun save(){
        //1. Name
        val name = binding.identityName.text.toString()

        if(name.isBlank()){
            snack(R.string.identity_needs_a_name)
            binding.identityName.requestFocus()
            return
        }

        //2. Uri
        val url = binding.identityUri.text.toString()
        if(url.isBlank()){
            snack(R.string.identity_needs_an_address)
            binding.identityName.requestFocus()
            return
        }else if(!url.startsWith("gemini://")){
            snack(R.string.identity_needs_a_gemini_address)
            binding.identityName.requestFocus()
            return
        }

        //3. Rule
        val rule = when(binding.ruleRadioGroup.checkedRadioButtonId){
            R.id.cert_unassigned -> IdentityRule.Unspecified().key
            R.id.cert_use_domain -> IdentityRule.EntireDomain().key
            R.id.cert_use_address -> IdentityRule.SpecificUrl().key
            else -> IdentityRule.Unspecified().key
        }

        if(isEdit){
            val updatedIdentity = Identity(
                name = name,
                alias = "${identity?.alias}",
                uri = Uri.parse(url),
                rule = rule,
                privateKey = identity?.privateKey ?: byteArrayOf()
            )
            identities.update(updatedIdentity){
                mainThread {
                    onDismiss(null, null)
                    dismiss()
                }
            }
        }else {

            //4. Create alias
            val alias =
                "ariane_${name}_$url".replace("gemini://", "").replace(" ", "_").replace(".", "_").lowercase()

            if(aliases.contains(alias)){
                snack(String.format(context.getString(R.string.alias_taken), alias))
            }else {

                thread {
                    CertGen.generate(
                        "${alias}_client_cert",
                        100
                    ) { keys, x509Certificate ->
                        println("CERTGEN new: $x509Certificate")
                        CertGen.addX509ToKeystore(alias, x509Certificate)

                        //At this point we have everything to store
                        identities.add(
                            Identity(
                                name = name,
                                alias = alias,
                                uri = Uri.parse(url),
                                rule = rule,
                                privateKey = keys.private.encoded
                            )
                        ) {
                            mainThread {
                                onDismiss(null, null)
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun snack(resId: Int){
        Snackbar.make(binding.root, context.getString(resId), Snackbar.LENGTH_SHORT).show()
    }

    private fun snack(message: String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}