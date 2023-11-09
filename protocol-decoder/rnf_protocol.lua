do
    --协议名称为RNF，在Packet Details窗格显示为QAX.TZ RNF
    local p_RNF = Proto("RNF","RNF Protocol")
    --协议的各个字段
    local f_identifier = ProtoField.uint8("RNF.identifier","Identifier", base.HEX)
    --这里的base是显示的时候的进制，详细可参考https://www.wireshark.org/docs/wsdg_html_chunked/lua_module_Proto.html#lua_class_ProtoField
	local f_packType = ProtoField.string("RNF.packType", "PackType", base.ASCII)
	local f_serializerCode = ProtoField.uint8("RNF.serializerCode", "SerializerCode", base.DEC)
    local f_length = ProtoField.uint8("RNF.length", "Length", base.DEC)
    local f_data = ProtoField.string("RNF.data", "Data", base.ASCII)

    --这里把RNF协议的全部字段都加到p_RNF这个变量的fields字段里
    p_RNF.fields = {f_identifier, f_packType, f_serializerCode, f_length, f_data}
    
	
    --这里是获取data这个解析器
    local data_dis = Dissector.get("data")
    
    local function RNF_dissector(buf,pkt,root)
        local buf_len = buf:len();
        --先检查报文长度，太短的不是我的协议
        if buf_len < 16 then return false end

        --验证一下identifier这个字段是不是0xCAFEBABE,如果不是的话，认为不是我要解析的packet
--         local v_identifier = buf(0, 4)
        local v_identifier = buf(0, 2)

--         if (v_identifier:uint() ~= 0xCAFEBABE)
        if (v_identifier:uint() ~= 0xBABE)
        then return false end

        --取出其他字段的值
--         local v_packType = buf(4, 4)
        local v_packType = buf(2, 1)
		print(v_packType)
		v_packType = tonumber(tostring(v_packType), 16)

		
-- 		local v_serializerCode = buf(8, 4)
		local v_serializerCode = buf(3, 1)
		v_serializerCode = tonumber(tostring(v_serializerCode), 16)
		
-- 		local v_length = buf(12, 4)
		local v_length = buf(4, 4)
		v_length = tonumber(tostring(v_length), 16)
		
		print("v_length")
		print(v_length)
		
--         local v_data = buf(16, v_length)
        local v_data = buf(8, v_length)

        --现在知道是我的协议了，放心大胆添加Packet Details
        local t = root:add(p_RNF,buf)
        --在Packet List窗格的Protocol列可以展示出协议的名称
        pkt.cols.protocol = "RNF"
        --这里是把对应的字段的值填写正确，只有t:add过的才会显示在Packet Details信息里. 所以在之前定义fields的时候要把所有可能出现的都写上，但是实际解析的时候，如果某些字段没出现，就不要在这里add
        t:add(f_identifier,v_identifier)
		t:add(f_packType,v_packType)
		t:add(f_serializerCode,v_serializerCode)
        t:add(f_length,v_length)
        t:add(f_data,v_data)
        
        return true
    end
    
    --这段代码是目的Packet符合条件时，被Wireshark自动调用的，是p_RNF的成员方法
    function p_RNF.dissector(buf, pkt, root) 
	    -- 设置一些UI中报文列表中Proto列和Info列显示的信息
		pkt.cols.protocol:set('RNF')
		pkt.cols.info:set('RNF Protocol')
        if RNF_dissector(buf,pkt,root) then
            --valid RNF diagram
        else
            --data这个dissector几乎是必不可少的；当发现不是我的协议时，就应该调用data
            data_dis:call(buf,pkt,root)
        end
    end
    
    local tcp_port_table = DissectorTable.get("tcp.port")
    --因为我们的自定义协议的接受端口是1314，所以这里只需要添加到"tcp.port"这个DissectorTable里，并且指定值为1314即可。
    tcp_port_table:add(9527, p_RNF)
end