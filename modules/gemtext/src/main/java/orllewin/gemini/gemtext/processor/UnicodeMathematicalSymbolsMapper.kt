package orllewin.gemini.gemtext.processor

/**
 * This class maps characters from the Mathematical Alphanumeric Symbols unicode block to standard A-Z a-z
 *
 * @see https://en.wikipedia.org/wiki/Mathematical_Alphanumeric_Symbols
 */
class UnicodeMathematicalSymbolsMapper {

    private val standard = listOf(
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z')

    private val highSurrogateCode = 55349

    private val alphaRange = IntRange(56320, 56995)

    //Serif Bold: 𝐀𝐁𝐂𝐃𝐄𝐅𝐆𝐇𝐈𝐉𝐊𝐋𝐌𝐍𝐎𝐏𝐐𝐑𝐒𝐓𝐔𝐕𝐖𝐗𝐘𝐙𝐚𝐛𝐜𝐝𝐞𝐟𝐠𝐡𝐢𝐣𝐤𝐥𝐦𝐧𝐨𝐩𝐪𝐫𝐬𝐭𝐮𝐯𝐰𝐱𝐲𝐳
    private val serifBoldRange = IntRange(56320, 56371)

    //Serif Italic: 𝐴𝐵𝐶𝐷𝐸𝐹𝐺𝐻𝐼𝐽𝐾𝐿𝑀𝑁𝑂𝑃𝑄𝑅𝑆𝑇𝑈𝑉𝑊𝑋𝑌𝑍𝑎𝑏𝑐𝑑𝑒𝑓𝑔ℎ𝑖𝑗𝑘𝑙𝑚𝑛𝑜𝑝𝑞𝑟𝑠𝑡𝑢𝑣𝑤𝑥𝑦𝑧
    private val serifItalicRange = IntRange(56372, 56423)

    //Serif Italic Bold: 𝑨𝑩𝑪𝑫𝑬𝑭𝑮𝑯𝑰𝑱𝑲𝑳𝑴𝑵𝑶𝑷𝑸𝑹𝑺𝑻𝑼𝑽𝑾𝑿𝒀𝒁𝒂𝒃𝒄𝒅𝒆𝒇𝒈𝒉𝒊𝒋𝒌𝒍𝒎𝒏𝒐𝒑𝒒𝒓𝒔𝒕𝒖𝒗𝒘𝒙𝒚𝒛
    private val serifItalicBoldRange = IntRange(56424, 56475)

    //Calligraphy Normal: 𝒜ℬ𝒞𝒟ℰℱ𝒢ℋℐ𝒥𝒦ℒℳ𝒩𝒪𝒫𝒬ℛ𝒮𝒯𝒰𝒱𝒲𝒳𝒴𝒵𝒶𝒷𝒸𝒹ℯ𝒻ℊ𝒽𝒾𝒿𝓀𝓁𝓂𝓃ℴ𝓅𝓆𝓇𝓈𝓉𝓊𝓋𝓌𝓍𝓎𝓏
    private val calligraphyNormalRange = IntRange(56476, 56527)

    //Calligraphy Bold: 𝓐𝓑𝓒𝓓𝓔𝓕𝓖𝓗𝓘𝓙𝓚𝓛𝓜𝓝𝓞𝓟𝓠𝓡𝓢𝓣𝓤𝓥𝓦𝓧𝓨𝓩𝓪𝓫𝓬𝓭𝓮𝓯𝓰𝓱𝓲𝓳𝓴𝓵𝓶𝓷𝓸𝓹𝓺𝓻𝓼𝓽𝓾𝓿𝔀𝔁𝔂𝔃
    private val calligraphyBoldRange = IntRange(56528, 56579)

    //Fraktur Normal: 𝔄𝔅ℭ𝔇𝔈𝔉𝔊ℌℑ𝔍𝔎𝔏𝔐𝔑𝔒𝔓𝔔ℜ𝔖𝔗𝔘𝔙𝔚𝔛𝔜ℨ𝔞𝔟𝔠𝔡𝔢𝔣𝔤𝔥𝔦𝔧𝔨𝔩𝔪𝔫𝔬𝔭𝔮𝔯𝔰𝔱𝔲𝔳𝔴𝔵𝔶𝔷
    private val frakturNormalRange = IntRange(56580, 56631)

    //Double Struck: 𝔸𝔹ℂ𝔻𝔼𝔽𝔾ℍ𝕀𝕁𝕂𝕃𝕄ℕ𝕆ℙℚℝ𝕊𝕋𝕌𝕍𝕎𝕏𝕐ℤ𝕒𝕓𝕔𝕕𝕖𝕗𝕘𝕙𝕚𝕛𝕜𝕝𝕞𝕟𝕠𝕡𝕢𝕣𝕤𝕥𝕦𝕧𝕨𝕩𝕪𝕫
    private val doubleStruckRange = IntRange(56632, 56683)

    //Fraktur Bold: 𝕬𝕭𝕮𝕯𝕰𝕱𝕲𝕳𝕴𝕵𝕶𝕷𝕸𝕹𝕺𝕻𝕼𝕽𝕾𝕿𝖀𝖁𝖂𝖃𝖄𝖅𝖆𝖇𝖈𝖉𝖊𝖋𝖌𝖍𝖎𝖏𝖐𝖑𝖒𝖓𝖔𝖕𝖖𝖗𝖘𝖙𝖚𝖛𝖜𝖝𝖞𝖟
    private val frakturBoldRange = IntRange(56684, 56735)

    //Sans Serif Normal: 𝖠𝖡𝖢𝖣𝖤𝖥𝖦𝖧𝖨𝖩𝖪𝖫𝖬𝖭𝖮𝖯𝖰𝖱𝖲𝖳𝖴𝖵𝖶𝖷𝖸𝖹𝖺𝖻𝖼𝖽𝖾𝖿𝗀𝗁𝗂𝗃𝗄𝗅𝗆𝗇𝗈𝗉𝗊𝗋𝗌𝗍𝗎𝗏𝗐𝗑𝗒𝗓
    private val sansNormalRange = IntRange(56736, 56787)

    //Sans Serif Bold: 𝗔𝗕𝗖𝗗𝗘𝗙𝗚𝗛𝗜𝗝𝗞𝗟𝗠𝗡𝗢𝗣𝗤𝗥𝗦𝗧𝗨𝗩𝗪𝗫𝗬𝗭𝗮𝗯𝗰𝗱𝗲𝗳𝗴𝗵𝗶𝗷𝗸𝗹𝗺𝗻𝗼𝗽𝗾𝗿𝘀𝘁𝘂𝘃𝘄𝘅𝘆𝘇
    private val sansBoldRange = IntRange(56788, 56839)

    //Sans Serif Italic: 𝘈𝘉𝘊𝘋𝘌𝘍𝘎𝘏𝘐𝘑𝘒𝘓𝘔𝘕𝘖𝘗𝘘𝘙𝘚𝘛𝘜𝘝𝘞𝘟𝘠𝘡𝘢𝘣𝘤𝘥𝘦𝘧𝘨𝘩𝘪𝘫𝘬𝘭𝘮𝘯𝘰𝘱𝘲𝘳𝘴𝘵𝘶𝘷𝘸𝘹𝘺𝘻
    private val sansItalicRange = IntRange(56840, 56891)

    //Sans Serif Italic Bold: 𝘼𝘽𝘾𝘿𝙀𝙁𝙂𝙃𝙄𝙅𝙆𝙇𝙈𝙉𝙊𝙋𝙌𝙍𝙎𝙏𝙐𝙑𝙒𝙓𝙔𝙕𝙖𝙗𝙘𝙙𝙚𝙛𝙜𝙝𝙞𝙟𝙠𝙡𝙢𝙣𝙤𝙥𝙦𝙧𝙨𝙩𝙪𝙫𝙬𝙭𝙮𝙯
    private val sansItalicBoldRange = IntRange(56892, 56943)

    //Monospace: 𝙰𝙱𝙲𝙳𝙴𝙵𝙶𝙷𝙸𝙹𝙺𝙻𝙼𝙽𝙾𝙿𝚀𝚁𝚂𝚃𝚄𝚅𝚆𝚇𝚈𝚉𝚊𝚋𝚌𝚍𝚎𝚏𝚐𝚑𝚒𝚓𝚔𝚕𝚖𝚗𝚘𝚙𝚚𝚛𝚜𝚝𝚞𝚟𝚠𝚡𝚢𝚣
    private val monospaceRange = IntRange(56944, 56995)


    fun hasMathematicalAlphanumericSymbols(text: String): Boolean =
        text.codePoints().anyMatch { codePoint ->
            Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.MATHEMATICAL_ALPHANUMERIC_SYMBOLS
        }

    fun remap(text: String): String {
        val remappedChars = mutableListOf<Char>()

        var highSurrogateCode = -1

        text.chars().forEach { code ->

            var char = Character.valueOf(Char(code))
            val isSurrogate = char.isSurrogate()

            if (isSurrogate) {
                when {
                    char.isHighSurrogate() -> highSurrogateCode = code
                    char.isLowSurrogate() -> {
                        if (highSurrogateCode == this.highSurrogateCode && alphaRange.contains(code)) {
                            when {
                                serifBoldRange.contains(code) -> char = standard[code - serifBoldRange.first]
                                serifItalicRange.contains(code) -> char = standard[code - serifItalicRange.first]
                                serifItalicBoldRange.contains(code) -> char = standard[code - serifItalicBoldRange.first]
                                sansNormalRange.contains(code) -> char = standard[code - sansNormalRange.first]
                                sansBoldRange.contains(code) -> char = standard[code - sansBoldRange.first]
                                sansItalicRange.contains(code) -> char = standard[code - sansItalicRange.first]
                                sansItalicBoldRange.contains(code) -> char = standard[code - sansItalicBoldRange.first]
                                calligraphyNormalRange.contains(code) -> char = standard[code - calligraphyNormalRange.first]
                                calligraphyBoldRange.contains(code) -> char = standard[code - calligraphyBoldRange.first]
                                frakturNormalRange.contains(code) -> char = standard[code - frakturNormalRange.first]
                                frakturBoldRange.contains(code) -> char = standard[code - frakturBoldRange.first]
                                monospaceRange.contains(code) -> char = standard[code - monospaceRange.first]
                                doubleStruckRange.contains(code) -> char = standard[code - doubleStruckRange.first]
                            }

                            remappedChars.add(char)
                        }else{
                            //Character is in this block but it's not one this class currently handles, so add high and low surrogates as-is
                            remappedChars.add(Character.valueOf(Char(highSurrogateCode)))
                            remappedChars.add(char)
                        }
                    }
                }
            } else {
                remappedChars.add(char)
            }
        }

        return String(remappedChars.toCharArray())
    }
}