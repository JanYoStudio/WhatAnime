package pw.janyo.whatanime.ui.fragment

//class AboutFragment : BasePreferenceFragment(R.xml.pref_about) {
//    private val clipboardManager: ClipboardManager by inject()
//    private val settingsViewModel: SettingsViewModel by viewModel()
//    private val languageArray by lazy { resources.getStringArray(R.array.language) }
//    private val requestTypeArray by lazy { resources.getStringArray(R.array.summary_request_type) }
//    private val progressDialog by lazy { ProgressDialog(requireActivity()) }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val deviceIdPreference: Preference = findPreferenceById(R.string.key_device_id)
//        val versionPreference: Preference = findPreferenceById(R.string.key_app_version)
//        val hideSexPreference: CheckBoxPreference = findPreferenceById(R.string.key_hide_sex)
//        val languagePreference: Preference = findPreferenceById(R.string.key_language)
//        val requestTypePreference: Preference = findPreferenceById(R.string.key_request_type)
//        val apiKeyPreference: EditTextPreference = findPreferenceById(R.string.key_api_key)
//        val checkApiKeyPreference: Preference = findPreferenceById(R.string.key_check_api_key)
//
//        deviceIdPreference.summary = publicDeviceId
//        languagePreference.summary = languageArray[Configure.language]
//        requestTypePreference.summary = requestTypeArray[Configure.requestType]
//        hideSexPreference.isChecked = Configure.hideSex
//        versionPreference.summary = BuildConfig.VERSION_NAME
//        apiKeyPreference.text = Configure.apiKey
//
//        settingsViewModel.refreshData.observe(requireActivity()) {
//            if (it) {
//                progressDialog.show()
//            } else {
//                progressDialog.dismiss()
//            }
//        }
//        settingsViewModel.searchQuota.observe(requireActivity()) { quota ->
//            MaterialAlertDialogBuilder(requireActivity())
//                .setTitle(R.string.title_check_api_key)
//                .setMessage(
//                    """
//                    Priority:       ${quota.priority}
//                    Concurrency:    ${quota.concurrency}
//                    Quota:          ${quota.quota}
//                    QuotaUsed:      ${quota.quotaUsed}
//                """.trimIndent()
//                )
//                .setPositiveButton(android.R.string.ok, null)
//                .setNegativeButton(R.string.action_donate) { _, _ ->
//                    requireActivity().loadInBrowser(Constant.donateUrl)
//                }
//                .setNeutralButton(R.string.action_about_quota) { _, _ ->
//                    requireActivity().loadInBrowser(Constant.quotaInfoUrl)
//                }
//                .show()
//        }
//
//        deviceIdPreference.setOnPreferenceClickListener {
//            val clipData =
//                ClipData.newPlainText(getString(R.string.app_name), deviceIdPreference.summary)
//            clipboardManager.setPrimaryClip(clipData)
//            toast(getString(R.string.hint_copy_device_id))
//            true
//        }
//        hideSexPreference.onPreferenceChangeListener =
//            Preference.OnPreferenceChangeListener { _, _ ->
//                Configure.hideSex = !hideSexPreference.isChecked
//                true
//            }
//        languagePreference.setOnPreferenceClickListener {
//            var select = Configure.language
//            val activity = requireActivity()
//            MaterialAlertDialogBuilder(activity)
//                .setTitle(R.string.title_change_language)
//                .setSingleChoiceItems(languageArray, select) { _, which ->
//                    select = which
//                }
//                .setPositiveButton(android.R.string.ok) { _, _ ->
//                    val needRestart = select != Configure.language
//                    Configure.language = select
//                    languagePreference.summary = languageArray[Configure.language]
//                    if (needRestart) {
//                        val intent =
//                            activity.packageManager.getLaunchIntentForPackage(activity.packageName)
//                        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                        activity.startActivity(intent)
//                        activity.finish()
//                    }
//                }
//                .setNegativeButton(android.R.string.cancel, null)
//                .show()
//            true
//        }
//        requestTypePreference.setOnPreferenceClickListener {
//            var select = Configure.requestType
//            val activity = requireActivity()
//            MaterialAlertDialogBuilder(activity)
//                .setTitle(R.string.title_request_type)
//                .setSingleChoiceItems(R.array.request_type, select) { _, which ->
//                    select = which
//                }
//                .setPositiveButton(android.R.string.ok) { _, _ ->
//                    Configure.requestType = select
//                    requestTypePreference.summary = requestTypeArray[Configure.requestType]
//                }
//                .setNegativeButton(android.R.string.cancel, null)
//                .show()
//            true
//        }
//        apiKeyPreference.setOnBindEditTextListener {
//            Configure.apiKey = it.text.toString()
//        }
//        checkApiKeyPreference.setOnPreferenceClickListener {
//            settingsViewModel.showQuota()
//            true
//        }
//
//        versionPreference.setOnPreferenceClickListener {
//            fastClick(5) {
//                debugMode.value = true
//                toastLong("调试模式已开启！")
//            }
//            true
//        }
//        findPreferenceById<Preference>(R.string.key_about_github).setOnPreferenceClickListener {
//            requireActivity().toCustomTabs(getString(R.string.link_about_github))
//            true
//        }
//        findPreferenceById<Preference>(R.string.key_about_license).setOnPreferenceClickListener {
//            requireActivity().toCustomTabs(getString(R.string.link_about_license))
//            true
//        }
//        findPreferenceById<Preference>(R.string.key_about_open_on_play).setOnPreferenceClickListener {
//            requireActivity().toCustomTabs(getString(R.string.link_about_open_on_play))
//            true
//        }
//        findPreferenceById<Preference>(R.string.key_janyo_license).setOnPreferenceClickListener {
//            if (Configure.useServerCompress)
//                requireActivity().toCustomTabs(getString(R.string.link_janyo_license))
//            else
//                requireActivity().toCustomTabs(getString(R.string.link_janyo_license_vip))
//            true
//        }
//        findPreferenceById<Preference>(R.string.key_developer_whatanime).setOnPreferenceClickListener {
//            requireActivity().toCustomTabs(getString(R.string.link_developer_whatanime))
//            true
//        }
//        findPreferenceById<Preference>(R.string.key_whatanime).setOnPreferenceClickListener {
//            requireActivity().toCustomTabs(getString(R.string.link_whatanime))
//            true
//        }
//    }
//}