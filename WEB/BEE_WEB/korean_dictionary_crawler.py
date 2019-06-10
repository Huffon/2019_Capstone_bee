#-*- coding: utf-8 -*-
import requests
from bs4 import BeautifulSoup

word = input()

url = "https://stdict.korean.go.kr/search/searchResult.do?&searchKeyword=" + word

response = requests.get(url)
soup = BeautifulSoup(response.text, "html.parser")

result = ""

try:
    result += soup.find('ul', {'class': 'result'}).find('dt').find( 'font', {'class': 'dataLine'}).get_text()
    result = result[1:]

except:
    result = "사전에 등재되어 있지 않습니다."
    
print(result)
