package orllewin.gemini.identity

sealed class IdentityRule{
    class EntireDomain(val key:String = "rule_entire_domain"): IdentityRule()
    class SpecificUrl(val key:String = "rule_specific_url"): IdentityRule()
    class Unspecified(val key:String = "rule_unspecified"): IdentityRule()
}
