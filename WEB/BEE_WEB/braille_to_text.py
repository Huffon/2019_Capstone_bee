#-*- coding: utf-8 -*-

import json
import hgtk

MATCH_H2B_CHO = {
    '04':'ㄱ',
    '36':'ㄴ',
    '20':'ㄷ',
    '02':'ㄹ',
    '34':'ㅁ',
    '06':'ㅂ',
    '01':'ㅅ',
    #'':'ㅇ',
    '05':'ㅈ',
    '03':'ㅊ',
    '52':'ㅋ',
    '50':'ㅌ',
    '38':'ㅍ',
    '22':'ㅎ',

    '0104':'ㄲ',
    '0120':'ㄸ',
    '0106':'ㅃ',
    '0101':'ㅆ',
    '0105':'ㅉ',
}

MATCH_H2B_JOONG = {
    '49':'ㅏ',
    '14':'ㅑ',
    '28':'ㅓ',
    '35':'ㅕ',
    '41':'ㅗ',
    '13':'ㅛ',
    '44':'ㅜ',
    '37':'ㅠ',
    '21':'ㅡ',
    '42':'ㅣ',
    '58':'ㅐ',
    '46':'ㅔ',
    '1458':'ㅒ',
    '12':'ㅖ',
    '57':'ㅘ',
    '5758':'ㅙ',
    '47':'ㅚ',
    '60':'ㅝ',
    '6058':'ㅞ',
    '4458':'ㅟ',
    '23':'ㅢ',
}

MATCH_H2B_JONG = {
    '32':'ㄱ',
    '18':'ㄴ',
    '10':'ㄷ',
    '16':'ㄹ',
    '17':'ㅁ',
    '48':'ㅂ',
    '08':'ㅅ',
    '27':'ㅇ',
    '40':'ㅈ',
    '24':'ㅊ',
    '26':'ㅋ',
    '25':'ㅌ',
    '19':'ㅍ',
    '11':'ㅎ',
    '3232':'ㄲ',
    '3208':'ㄳ',
    '1840':'ㄵ',
    '1811':'ㄶ',
    '1632':'ㄺ',
    '1617':'ㄻ',
    '1648':'ㄼ',
    '1608':'ㄽ',
    '1625':'ㄾ',
    '1611':'ㅀ',
    '4808':'ㅄ',
    '12':'ㅆ',
}
"""
MATCH_H2B_ECT = {
    '1': ['15', '32'],
    '2': ['15', '48'],
    '3': ['15', '36'],
    '4': ['15', '38'],
    '5': ['15', '34'],
    '6': ['15', '52'],
    '7': ['15', '54'],
    '8': ['15', '50'],
    '9': ['15', '20'],
    '0': ['15', '22'],

    ',': '16',
    '.': '19',
    '-': '18',
    '?': '23',
    '_': '9',
    '!': '26',
}
"""
fastest_handler= {'3228':'그래서',
                    '3236':'그러나',
                    '3218':'그러면',
                    '3217':'그러므로',
                    '3246':'그런데',
                    '3241':'그리고',
                    '3235':'그리하여',
                    '0728':'것'}

second_handler= {'53':'가',
                    '36':'나',
                    '20':'다',
                    '34':'마',
                    '06':'바',
                    '56':'사',
                    '05':'자',
                    '52':'카',
                    '50':'타',
                    '38':'파',
                    '22':'하',
                    '39':'억',
                    '31':'언',
                    '30':'얼',
                    '33':'연',
                    '51':'열',
                    '55':'영',
                    '45':'옥',
                    '59':'온',
                    '63':'옹',
                    '54':'운',
                    '61':'울',
                    '43':'은',
                    '29':'을',
                    '62':'인'}


def compose(braille):
    result=''
    # 맨 마지막에 들어온 '!' 제거
    if(braille[-1]=='!'):
        braille=braille[:-1]
    # !를 기준으로 점자정보 쪼개기
    braille=braille.split('!')

    for character in braille:
        text=''
        letter = []
        # 점자 정보가 2종약자
        if(character in fastest_handler):
            text=fastest_handler[character]
        # 점자 정보에 초성이 포함됨 (초성이 'ㅇ'이 아님)
        elif(character[0:2] in MATCH_H2B_CHO):
           # 초성이 쌍자음
            if(character[0:4] in MATCH_H2B_CHO):
                letter.append(MATCH_H2B_CHO[character[0:4]])
                character=character[4:]
            # 초성이 단자음
            else:
                letter.append(MATCH_H2B_CHO[character[0:2]])
                character=character[2:]
            # 중성과 종성이 1종 약자
            if(character[0:2] in second_handler):
                tmp=hgtk.text.decompose(second_handler[character])
                tmp = \
                    tmp.replace(hgtk.text.DEFAULT_COMPOSE_CODE, '')
                letter.append(tmp[1])
                letter.append(tmp[2])
            # 중성이 따로 있는 경우(초성+종성 1종약자가 아닌 경우)
            elif(character[0:2] in MATCH_H2B_JOONG):
                # 중성이 겹모음인 경우
                if(character[0:4] in MATCH_H2B_JOONG):
                    letter.append(MATCH_H2B_JOONG[character[0:4]])
                    character=character[4:]
                # 중성이 단모음인 경우
                else:
                    letter.append(MATCH_H2B_JOONG[character[0:2]])
                    character=character[2:]
                # 종성이 있는 경우
                if(character in MATCH_H2B_JONG):
                    letter.append(MATCH_H2B_JONG[character])
            # 중성이 따로 없음 (초성 + 중성 1종약자)
            else:
                letter.append('ㅏ')
                # 종성이 있는 경우
                if(character in MATCH_H2B_JONG):
                    letter.append(MATCH_H2B_JONG[character])

        # 1종약자로 시작되는 경우
        elif(character[0:2] in second_handler):
            tmp = hgtk.text.decompose(second_handler[character[0:2]])
            tmp = \
                tmp.replace(hgtk.text.DEFAULT_COMPOSE_CODE, '')
            # 1종약자가 '가'와 '사'인 경우
            if((character[0:2] == '53')or(character[0:2] == '56')):
                letter.append(tmp[0])
                letter.append(tmp[1])
                character=character[2:]
            # 그 외의 1종약자인 경우
            else:
                letter.append(tmp[0])
                letter.append(tmp[1])
                letter.append(tmp[2])

            if(character in MATCH_H2B_JONG):
                letter.append(MATCH_H2B_JONG[character])
        # 점자의 초성이 'ㅇ'인 경우
        else:
            if(character[0:2] in second_handler):
                text=second_handler[character]
            elif(character[0:2] in MATCH_H2B_JOONG):
                letter.append('ㅇ')
                if(character[0:4] in MATCH_H2B_JOONG):
                    letter.append(MATCH_H2B_JOONG[character[0:4]])
                    character=character[4:]
                else:
                    letter.append(MATCH_H2B_JOONG[character[0:2]])
                    character=character[2:]

                if(character in MATCH_H2B_JONG):
                    letter.append(MATCH_H2B_JONG[character])
        #letter=(",".join(repr(e) for e in letter))

        if(character=='00'):
            text=' '
        if(text==''):
            if(int(len(letter))==3):
                text=hgtk.letter.compose(letter[0],letter[1],letter[2])
            else:
                text=hgtk.letter.compose(letter[0],letter[1])

        result+=text

    return result

def lambda_handler(event, context):
    braille = event["braille"]
    result = compose(braille)
    return {
        'statusCode': 200,
        'body': json.dumps(result)
    }
