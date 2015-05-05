q_matrix = load('out.csv');
q_matrix = q_matrix(:, 2:37);
util_matrix = load('mapChart.csv');
util_matrix = util_matrix(:,2:37);

util_vector = util_matrix(458,:);

rms = zeros(10000, 1); 
for i = 1 : 10000 
    for j = 1:36
        rms(i) = rms(i) + (q_matrix(i,j) - util_vector(j))^2;
    end
end

rms = rms ./ 36;

for i = 1: 10000
    rms(i) = sqrt(rms(i));
end

figure
loglog(rms)

