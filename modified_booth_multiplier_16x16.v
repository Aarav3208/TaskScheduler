module Multiplier_Hybrid_adder (
    input [15:0] multiplicand,
    input [15:0] multiplier,
    output [31:0] product
);

wire [16:0] extended_multiplier;
assign extended_multiplier = {1'b0, multiplier};

wire [31:0] partial_products [0:7];
wire [2:0] booth_controls [0:7];
wire [31:0] csa_sum [0:4];
wire [31:0] csa_carry [0:4];

genvar i;
generate
    for (i = 0; i < 8; i = i + 1) begin : modified_booth_gen
        wire [2:0] booth_bits;
        if (i == 0) begin
            assign booth_bits = {extended_multiplier[1], extended_multiplier[0], 1'b0};
        end else begin
            assign booth_bits = {extended_multiplier[2*i+1], extended_multiplier[2*i], extended_multiplier[2*i-1]};
        end
        
        modified_booth_encoder mbe (
            .booth_bits(booth_bits),
            .control(booth_controls[i])
        );
        
        wire [31:0] multiplicand_extended;
        wire [31:0] selected_value;
        
        assign multiplicand_extended = {{16{multiplicand[15]}}, multiplicand};
        
        assign selected_value = (booth_controls[i] == 3'b000) ? 32'b0 :
                               (booth_controls[i] == 3'b001) ? multiplicand_extended :
                               (booth_controls[i] == 3'b010) ? (multiplicand_extended << 1) :
                               (booth_controls[i] == 3'b101) ? (~multiplicand_extended + 1) :
                               (booth_controls[i] == 3'b110) ? (~(multiplicand_extended << 1) + 1) :
                               32'b0;
        
        assign partial_products[i] = selected_value << (2*i);
    end
endgenerate

csa_32bit csa1_1 (
    .a(partial_products[0]), 
    .b(partial_products[1]), 
    .c(partial_products[2]), 
    .sum(csa_sum[0]), 
    .carry(csa_carry[0])
);

csa_32bit csa1_2 (
    .a(partial_products[3]), 
    .b(partial_products[4]), 
    .c(partial_products[5]), 
    .sum(csa_sum[1]), 
    .carry(csa_carry[1])
);

csa_32bit csa2_1 (
    .a(csa_sum[0]), 
    .b({csa_carry[0][30:0], 1'b0}), 
    .c(csa_sum[1]), 
    .sum(csa_sum[2]), 
    .carry(csa_carry[2])
);

csa_32bit csa2_2 (
    .a({csa_carry[1][30:0], 1'b0}), 
    .b(partial_products[6]), 
    .c(partial_products[7]), 
    .sum(csa_sum[3]), 
    .carry(csa_carry[3])
);

csa_32bit csa3_1 (
    .a(csa_sum[2]), 
    .b({csa_carry[2][30:0], 1'b0}), 
    .c(csa_sum[3]), 
    .sum(csa_sum[4]), 
    .carry(csa_carry[4])
);

assign product = csa_sum[4] + {csa_carry[4][30:0], 1'b0} + {csa_carry[3][30:0], 1'b0};

endmodule

module modified_booth_encoder (
    input [2:0] booth_bits,
    output reg [2:0] control
);

always @(*) begin
    case (booth_bits)
        3'b000: control = 3'b000;
        3'b001: control = 3'b001;
        3'b010: control = 3'b001;
        3'b011: control = 3'b010;
        3'b100: control = 3'b110;
        3'b101: control = 3'b101;
        3'b110: control = 3'b101;
        3'b111: control = 3'b000;
        default: control = 3'b000;
    endcase
end

endmodule

module csa_32bit (
    input [31:0] a, b, c,
    output [31:0] sum, carry
);

genvar i;
generate
    for (i = 0; i < 32; i = i + 1) begin : csa_bit
        assign sum[i] = a[i] ^ b[i] ^ c[i];
        assign carry[i] = (a[i] & b[i]) | (b[i] & c[i]) | (c[i] & a[i]);
    end
endgenerate

endmodule
