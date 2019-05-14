import hgtk

MATCH_H2B_CHO = {
    u'ㄱ': '04', u'ㄴ': '36', u'ㄷ': '20', u'ㄹ': '02', u'ㅁ': '34',
    u'ㅂ': '06', u'ㅅ': '01', u'ㅇ': '54', u'ㅈ': '05', u'ㅊ': '03',
    u'ㅋ': '52', u'ㅌ': '50', u'ㅍ': '38', u'ㅎ': '22',

    u'ㄲ': '0104', u'ㄸ':'0120', u'ㅃ': '0106',
    u'ㅆ': '0101', u'ㅉ': '0105',
}

MATCH_H2B_JOONG = {
    u'ㅏ': '49', u'ㅑ': '14', u'ㅓ': '28', u'ㅕ': '35', u'ㅗ': '41',
    u'ㅛ': '13', u'ㅜ': '44', u'ㅠ': '37', u'ㅡ': '21', u'ㅣ': '42',
    u'ㅐ': '58', u'ㅔ': '46', u'ㅖ': '12', u'ㅢ': '23', u'ㅘ': '57',
    u'ㅚ': '47', u'ㅝ': '60',

    u'ㅙ': '5758', u'ㅒ': '1458', u'ㅞ': '6058', u'ㅟ': '4458',

}

MATCH_H2B_JONG = {
    u'ㄱ': '32', u'ㄴ': '18', u'ㄷ': '10', u'ㄹ': '16', u'ㅁ': '17',
    u'ㅂ': '48', u'ㅅ': '08', u'ㅇ': '27', u'ㅈ': '40', u'ㅊ': '24',
    u'ㅋ': '26', u'ㅌ': '25', u'ㅍ': '19', u'ㅎ': '11',

    u'ㄲ': '3232', u'ㄳ': '3208', u'ㄵ': '1840',
    u'ㄶ': '1811', u'ㄺ': '1632', u'ㄻ': '1617',
    u'ㄼ': '1648', u'ㄽ': '1608', u'ㄾ': '1625',
    u'ㅀ': '1611', u'ㅄ': '4808',

    u'ㅆ': '12',
}

MATCH_H2B_ECT = {
    '1': '1532', '2': '1548', '3': '1536', '4': '1538', '5': '1534',
    '6': '1552', '7': '1554', '8': '1550', '9': '1520', '0': '1522',

    ',': '16', '.': '19', '-': '18', '?': '23', '_': '09', '!': '26',
}

def fastest_handler(fast_text):
    abbreviation = {'그래서': '3228',
                    '그러나': '3236',
                    '그러면': '3218',
                    '그러므로': '3217',
                    '그런데': '3246',
                    '그리고': '3241',
                    '그리하여': '3235',
                    '것': '0728'}

    if fast_text in abbreviation:
        return abbreviation[fast_text]
    else:
        return False

def second_handler(second_text):
    abbreviation = {'가': '53', '나': '36', '다': '20', '마': '34',
                    '바': '06', '사': '56', '자': '05', '카': '52',
                    '타': '50', '파': '38', '하': '22',

                    '억': '39', '언': '31', '얼': '30', '연': '33',
                    '열': '51', '영': '55', '옥': '45', '온': '59',
                    '옹': '63', '운': '54', '울': '61', '은': '43',
                    '을': '29', '인': '62',
                    }

    if second_text in abbreviation:
        return abbreviation[second_text]
    else:
        return False

def letter(hangul_letter):
    result = ""

    hangul_decomposed = hgtk.text.decompose(hangul_letter[0])
    hangul_decomposed = \
        hangul_decomposed.replace(hgtk.text.DEFAULT_COMPOSE_CODE, '')
    cho=''
    jung=''
    jong=''
    for i in range(len(hangul_decomposed)):
        hangul = hangul_decomposed[i]
        if i == 0 and hangul in MATCH_H2B_CHO:
            cho = MATCH_H2B_CHO[hangul]
        if i == 0 and hangul in MATCH_H2B_ECT:
            cho = MATCH_H2B_ECT[hangul]
        if i == 1 and hangul in MATCH_H2B_JOONG:
            jung = MATCH_H2B_JOONG[hangul]
        #종성이 있는 경우
        if i == 2 and hangul in MATCH_H2B_JONG:
            jong = MATCH_H2B_JONG[hangul]
            #초성+중성 1종약자
            if (second_handler(hgtk.letter.compose(hangul_decomposed[0], hangul_decomposed[1]))):
                result = second_handler(hgtk.letter.compose(hangul_decomposed[0], hangul_decomposed[1])) + jong
            #중성+종성 1종약자
            elif (second_handler(hgtk.letter.compose('ㅇ', hangul_decomposed[1], hangul_decomposed[2]))):
                result = cho + second_handler(hgtk.letter.compose('ㅇ', hangul_decomposed[1], hangul_decomposed[2]))
            #1종약자 미포함
            else:
                result = cho + jung + jong
        #종성이 없는 경우
        else:
            jong=''
            #초성+중성 1종약자
            if (second_handler(hgtk.letter.compose(hangul_decomposed[0], hangul_decomposed[1]))):
                result = second_handler(hgtk.letter.compose(hangul_decomposed[0], hangul_decomposed[1]))
            #1종약자 미포함
            else:
                result = cho + jung
    if result == []:
        result += '00'
    return result


def main(text):
    result = ""
    text = text.split(" ")

    # 이종약자 규칙 먼저 걸러줌
    for elem in text:
        # 이종약자 규칙 함수 호출
        fast_result = fastest_handler(elem)

        # 이종약자 규칙에 걸렸으면 해당 반환값 전체 result에 추가
        if fast_result != False:
            result += fast_result

        # 이종약자 규칙에 안걸렸으면 문자 단위 분석 수행
        else:
            for hangul_letter in elem:
                result+= letter(hangul_letter)

    return result


print(main("양"))
