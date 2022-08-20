from sys import argv
from glob import glob
from base64 import b64decode
from zlib import decompress, MAX_WBITS

HEX_FROM = '000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F'\
           '202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F'\
           '404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F'\
           '606162636465666768696A6B6C6D6E6F707172737475767778797A7B7C7D7E7F'\
           '808182838485868788898A8B8C8D8E8F909192939495969798999A9B9C9D9E9F'\
           'A0A1A2A3A4A5A6A7A8A9AAABACADAEAFB0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF'\
           'C0C1C2C3C4C5C6C7C8C9CACBCCCDCECFD0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF'\
           'E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEFF0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF'
HEX_TO   = '3D6585B318DBE287F152AB634BB5A05F7D687B9B24C228678ADEA4261E03EB17'\
           '6F343E7A3FD2A96A0FE935561FB14D1078D975F6BC4104816106F9ADD6D5297E'\
           '869E79E505BA84CC6E278EB05DA8F39FD0A271B858DD2C38994C480755E4538C'\
           '46B62DA5AF322240DC50C3A1258B9C16605CCFFD0C981CD4376D3C3A30E86C31'\
           '47F533DA43C8E35E1994ECE6A39514E09D64FA5915C52FCABB0BDFF297BF0A76'\
           'B449445A1DF0009621807F1A82394FC1A7D70DD1D8FF139370EE5BEFBE09B977'\
           '72E7B254B72AC7739066200E51EDF87C8F2EF412C62B83CDACCB3BC44EC06936'\
           '6202AE88FCAA4208A64557D39ABDE1238D924A1189746B91FBFEC901EA1BF7CE'

if __name__ == '__main__':
    print('-= UnWrap by Malgo=-')

    hex_from = [int(HEX_FROM[i:i + 2], 16) for i in range(0, len(HEX_FROM), 2)]
    hex_to   = [(int(HEX_TO[i:i + 1], 16) << 4) + int(HEX_TO[i + 1:i + 2], 16) for i in range(0, len(HEX_TO), 2)]

    for file in glob(argv[1] if len(argv) > 1 else '*.pbl'):
        with open(file + '.sql', 'wt', encoding='latin-1') as fout:
            with open(file, 'rt') as fin:
                for line in fin:
                    if line.startswith('a000000'):
                        for _ in range(17):
                            next(fin)
                        length = int(next(fin).split(' ')[1], 16)
                        data_base64 = ''
                        while length > 0:
                            line = next(fin).rstrip('\r\n')
                            data_base64 += line
                            length -= 1 + len(line)

                        data_translated = []
                        for b in b64decode(data_base64)[20:]:
                            data_translated.append([hex_to[i] for i, ch in enumerate(hex_from) if ch == b][0])

                        print(str(decompress(bytes(data_translated), MAX_WBITS).decode('latin-1')), file=fout)
                    else:
                        print(line.replace('wrapped', ''), file=fout)
        print(file, '->', file + '.sql')
